package snownee.lychee.mixin.recipes.anvilcrafting;

import org.jspecify.annotations.Nullable;
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
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LootContextKeys;
import snownee.lychee.RecipeTypes;
import snownee.lychee.context.ActionContext;
import snownee.lychee.context.AnvilContext;
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
	private @Nullable LycheeContext context;
	@Unique
	private @Nullable Pair<LycheeContext, ActionContext> onTakeResult;

	private AnvilMenuMixin(
			@Nullable final MenuType<?> menuType,
			final int containerId,
			final Inventory inventory,
			final ContainerLevelAccess access,
			final ItemCombinerMenuSlotDefinition itemInputSlots
	) {
		super(menuType, containerId, inventory, access, itemInputSlots);
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
		final var lootParams = context.initLootParams(RecipeTypes.ANVIL_CRAFTING);
		BlockPos pos = access.evaluate((level, pos0) -> pos0).orElseGet(player::blockPosition);
		lootParams.set(LootContextParams.ORIGIN, Vec3.atCenterOf(pos));
		if (access != ContainerLevelAccess.NULL) {
			lootParams.set(LootContextKeys.BLOCK_POS, pos);
			lootParams.set(LootContextParams.BLOCK_STATE, player.level().getBlockState(pos));
		}
		lootParams.set(LootContextParams.THIS_ENTITY, player);
		lootParams.validate();
		// why use copy(): vanilla will modify the originals
		context.put(
				LycheeContextKey.ITEM,
				ItemStackHolderCollection.Inventory.of(context, left.copy(), right.copy(), ItemStack.EMPTY)
		);
		RecipeTypes.ANVIL_CRAFTING.findFirst(context, player.level()).ifPresent(it -> {
			context.put(it);
			final var output = it.value().assemble(context);
			if (output.isEmpty()) {
				resultSlots.setItem(0, ItemStack.EMPTY);
				cost.set(0);
				context = null;
			} else {
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
	private void lychee_onTake(Player player, ItemStack carried, CallbackInfo ci) {
		if (context == null) {
			return;
		}
		if (context.level().isClientSide()) {
			return;
		}
		var recipe = context.getOrNull(LycheeContextKey.RECIPE);
		if (recipe == null) {
			return;
		}
		ActionContext actionContext = recipe.applyPostActions(context, 1);
		if (actionContext != null) {
			context.get(LycheeContextKey.ITEM).postApply(false, 1);
			onTakeResult = Pair.of(context, actionContext);
		}
	}

	@Inject(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/inventory/ContainerLevelAccess;execute" +
							"(Ljava/util/function/BiConsumer;)V"
			), method = "onTake", cancellable = true
	)
	private void lychee_preventDefault(Player player, ItemStack carried, CallbackInfo ci) {
		if (onTakeResult != null) {
			for (int i = 0; i < 2; i++) {
				ItemStackHolderCollection holders = onTakeResult.getFirst().get(LycheeContextKey.ITEM);
				if (holders.get(i).getConsumption() == 0) {
					inputSlots.setItem(i, holders.get(i).get());
				}
			}

			boolean avoidDefault = onTakeResult.getSecond().avoidDefault;
			onTakeResult = null;
			if (avoidDefault) {
				access.execute((level, pos) -> level.levelEvent(LevelEvent.SOUND_ANVIL_USED, pos, 0));
				ci.cancel();
			}
		}
	}
}
