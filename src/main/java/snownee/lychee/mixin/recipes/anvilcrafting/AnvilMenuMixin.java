package snownee.lychee.mixin.recipes.anvilcrafting;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.RecipeTypes;
import snownee.lychee.context.AnvilContext;
import snownee.lychee.context.RecipeContext;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.input.ItemStackHolderCollection;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {

	@Shadow
	private int repairItemCountCost;
	@Shadow
	private String itemName;
	@Final
	@Shadow
	private DataSlot cost;
	@Unique
	private LycheeContext context;
	@Unique
	private LycheeContext onTakeCtx;

	private AnvilMenuMixin(
			@Nullable final MenuType<?> type,
			final int containerId,
			final Inventory playerInventory,
			final ContainerLevelAccess access
	) {
		super(type, containerId, playerInventory, access);
	}


	@Inject(at = @At("HEAD"), method = "createResult", cancellable = true)
	private void lychee_createResult(CallbackInfo ci) {
		context = null;
		if (RecipeTypes.ANVIL_CRAFTING.isEmpty()) {
			return;
		}
		final var left = inputSlots.getItem(0);
		if (left.isEmpty()) {
			return;
		}
		final var right = inputSlots.getItem(1);
		context = new LycheeContext();
		context.put(LycheeContextKey.LEVEL, player.level());
		final var anvilContext = new AnvilContext(Pair.of(left, right), itemName);
		context.put(LycheeContextKey.ANVIL, anvilContext);
		final var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		BlockPos pos = access.evaluate((level, pos0) -> pos0).orElseGet(player::blockPosition);
		lootParamsContext.setParam(LootContextParams.ORIGIN, Vec3.atCenterOf(pos));
		if (access != ContainerLevelAccess.NULL) {
			lootParamsContext.setParam(LycheeLootContextParams.BLOCK_POS, pos);
			lootParamsContext.setParam(LootContextParams.BLOCK_STATE, player.level().getBlockState(pos));
		}
		lootParamsContext.setParam(LootContextParams.THIS_ENTITY, player);
		lootParamsContext.validate(RecipeTypes.ANVIL_CRAFTING.contextParamSet);
		// why use copy(): vanilla will modify the originals
		context.put(
				LycheeContextKey.ITEM,
				ItemStackHolderCollection.Inventory.of(context, left.copy(), right.copy(), ItemStack.EMPTY)
		);
		RecipeTypes.ANVIL_CRAFTING.findFirst(context, player.level()).ifPresent(it -> {
			context.put(LycheeContextKey.RECIPE_ID, new RecipeContext(it.id()));
			final var output = it.value().assemble(context, player.level().registryAccess());
			if (output.isEmpty()) {
				resultSlots.setItem(0, ItemStack.EMPTY);
				cost.set(0);
				context = null;
			} else {
				context.put(LycheeContextKey.RECIPE, it.value());
				resultSlots.setItem(0, output);
				if (player.isCreative() || left.getCount() == 1) {
					cost.set(anvilContext.getLevelCost());
				} else {
					// Anvil will swallow all items on the left
					// Make it too expensive so player knows the recipe is working
					// ClientboundContainerSetDataPacket only send short.
					cost.set(Short.MAX_VALUE);
				}
				repairItemCountCost = anvilContext.getMaterialCost();
			}
			broadcastChanges();
			ci.cancel();
		});
	}

	@Inject(at = @At("HEAD"), method = "onTake")
	private void lychee_onTake(Player player, ItemStack stack, CallbackInfo ci) {
		if (context == null) {
			return;
		}
		if (context.get(LycheeContextKey.LEVEL).isClientSide) {
			return;
		}
		var recipe = context.get(LycheeContextKey.RECIPE);
		if (recipe == null) {
			return;
		}
		onTakeCtx = context;
		recipe.applyPostActions(context, 1);
	}

	@Inject(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/inventory/ContainerLevelAccess;execute" +
							"(Ljava/util/function/BiConsumer;)V"
			), method = "onTake", cancellable = true
	)
	private void lychee_preventDefault(Player player, ItemStack stack, CallbackInfo ci) {
		if (onTakeCtx != null) {
			for (int i = 0; i < 2; i++) {
				if (onTakeCtx.get(LycheeContextKey.ITEM).get(i).getIgnoreConsumption()) {
					inputSlots.setItem(i, onTakeCtx.get(LycheeContextKey.ITEM).get(i).get());
				}
			}

			boolean avoidDefault = onTakeCtx.get(LycheeContextKey.ACTION).avoidDefault;
			onTakeCtx = null;
			if (avoidDefault) {
				access.execute((level, pos) -> level.levelEvent(LevelEvent.SOUND_ANVIL_USED, pos, 0));
				ci.cancel();
			}
		}
	}
}
