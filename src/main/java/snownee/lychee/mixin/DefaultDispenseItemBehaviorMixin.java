package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.Fallable;
import snownee.lychee.LycheeConfig;
import snownee.lychee.LycheeTags;
import snownee.lychee.util.CommonProxy;

@Mixin(DefaultDispenseItemBehavior.class)
public class DefaultDispenseItemBehaviorMixin {

	@Inject(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/item/ItemStack;split(I)Lnet/minecraft/world/item/ItemStack;"
			), method = "execute", locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true
	)
	private void execute(
			BlockSource pSource,
			ItemStack pStack,
			CallbackInfoReturnable<ItemStack> ci,
			Direction direction,
			Position position
	) {
		if (this == DropperBlock.DISPENSE_BEHAVIOUR) {
			return;
		}
		if (!(pStack.getItem() instanceof BlockItem item)) {
			return;
		}
		var block = item.getBlock();
		if (!(
				pStack.is(LycheeTags.DISPENSER_PLACEMENT) ||
						LycheeConfig.dispenserFallableBlockPlacement && block instanceof Fallable)) {
			return;
		}
		ci.setReturnValue(CommonProxy.dispensePlacement(pSource, pStack, direction));
	}

}
