package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.LycheeConfig;
import snownee.lychee.LycheeTags;

@Mixin(DefaultDispenseItemBehavior.class)
public class DefaultDispenseItemBehaviorMixin {

	@Inject(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;split(I)Lnet/minecraft/world/item/ItemStack;"
			), method = "execute", locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true
	)
	private void execute(BlockSource pSource, ItemStack pStack, CallbackInfoReturnable<ItemStack> ci, Direction direction, Position position) {
		Item item = pStack.getItem();
		if (!(item instanceof BlockItem)) {
			return;
		}
		Block block = ((BlockItem) item).getBlock();
		if (!(pStack.is(LycheeTags.DISPENSER_PLACEMENT) || LycheeConfig.dispenserFallableBlockPlacement && block instanceof Fallable)) {
			return;
		}
		BlockPos blockpos = pSource.getPos().relative(direction);
		BlockState state = pSource.getLevel().getBlockState(blockpos);
		if (FallingBlock.isFree(state)) {
			((BlockItem) item).place(new DirectionalPlaceContext(pSource.getLevel(), blockpos, direction, pStack, direction));
		}
		ci.setReturnValue(pStack);
	}

}