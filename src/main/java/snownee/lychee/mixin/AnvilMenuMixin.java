package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.RecipeTypes;
import snownee.lychee.anvil_crafting.AnvilContext;
import snownee.lychee.anvil_crafting.AnvilCraftingRecipe;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {

	@Shadow
	public int repairItemCountCost;
	@Shadow
	private String itemName;
	@Shadow
	private DataSlot cost;

	private AnvilCraftingRecipe lycheeRecipe;
	private AnvilContext lycheeCtx;
	private boolean lycheePreventDefault;

	public AnvilMenuMixin(MenuType<?> p_39773_, int p_39774_, Inventory p_39775_, ContainerLevelAccess p_39776_) {
		super(p_39773_, p_39774_, p_39775_, p_39776_);
	}

	@Inject(at = @At("HEAD"), method = "createResult", cancellable = true)
	private void lychee_createResult(CallbackInfo ci) {
		lycheeRecipe = null;
		lycheeCtx = null;
		if (RecipeTypes.ANVIL_CRAFTING.isEmpty()) {
			return;
		}
		ItemStack left = inputSlots.getItem(0);
		if (left.isEmpty()) {
			return;
		}
		ItemStack right = inputSlots.getItem(1);
		AnvilContext.Builder builder = new AnvilContext.Builder(player.level, left, right, itemName);
		BlockPos pos = access.evaluate((level, pos0) -> pos0).orElseGet(player::blockPosition);
		builder.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos));
		if (access != ContainerLevelAccess.NULL) {
			builder.withOptionalParameter(LycheeLootContextParams.BLOCK_POS, pos);
			builder.withOptionalParameter(LootContextParams.BLOCK_STATE, player.level.getBlockState(pos));
		}
		builder.withParameter(LootContextParams.THIS_ENTITY, player);
		AnvilContext ctx = builder.create(RecipeTypes.ANVIL_CRAFTING.contextParamSet);
		RecipeTypes.ANVIL_CRAFTING.findFirst(ctx, player.level).ifPresent($ -> {
			ItemStack output = $.assemble(ctx);
			if (output.isEmpty()) {
				resultSlots.setItem(0, ItemStack.EMPTY);
				cost.set(0);
			} else {
				lycheeRecipe = $;
				lycheeCtx = ctx;
				resultSlots.setItem(0, output);
				if (player.isCreative() || left.getCount() == 1) {
					cost.set(ctx.levelCost);
				} else { // Anvil will swallow all items on the left
					// Make it too expensive so player knows the recipe is working
					cost.set(Integer.MAX_VALUE);
				}
				repairItemCountCost = ctx.materialCost;
			}
			broadcastChanges();
			ci.cancel();
		});
	}

	@Inject(at = @At("HEAD"), method = "onTake")
	private void lychee_onTake(Player player, ItemStack stack, CallbackInfo ci) {
		if (lycheeRecipe == null) {
			lycheePreventDefault = false;
		} else {
			lycheePreventDefault = !lycheeRecipe.applyPostActions(lycheeCtx, 1);
		}
	}

	@Inject(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/world/inventory/ContainerLevelAccess;execute(Ljava/util/function/BiConsumer;)V"
			), method = "onTake", cancellable = true
	)
	private void lychee_preventDefault(Player player, ItemStack stack, CallbackInfo ci) {
		if (lycheePreventDefault) {
			lycheePreventDefault = false;
			access.execute((level, pos) -> level.levelEvent(1030, pos, 0));
			ci.cancel();
		}
	}

}
