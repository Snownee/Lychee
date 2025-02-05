package snownee.lychee.mixin;

import java.util.function.Predicate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.random_block_ticking.RandomlyTickable;
import snownee.lychee.util.BlockStateSet;

@Mixin(Block.class)
public class BlockMixin implements RandomlyTickable {

	@Unique
	private Predicate<BlockState> lychee$randomlyTickable = BlockStateSet.NONE;

	@Inject(at = @At("HEAD"), method = "isRandomlyTicking", cancellable = true)
	private void isRandomlyTicking(BlockState blockState, CallbackInfoReturnable<Boolean> ci) {
		if (lychee$isTickable(blockState)) {
			ci.setReturnValue(true);
		}
	}

	@Override
	public void lychee$setTickable(Predicate<BlockState> randomlyTickable) {
		lychee$randomlyTickable = randomlyTickable;
	}

	@Override
	public boolean lychee$isTickable(BlockState blockState) {
		return lychee$randomlyTickable.test(blockState);
	}
}
