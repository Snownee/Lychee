package snownee.lychee.mixin.recipes.dripstone;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.recipes.DripstoneRecipe;
import snownee.lychee.util.CommonProxy;

/**
 * No DF lib for now 2024/2/28
 */
@Mixin(BlockStateBase.class)
public class BlockStateMixin {

	@Inject(at = @At("HEAD"), method = "randomTick", cancellable = true)
	private void randomTick(ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, CallbackInfo ci) {
		BlockState state = (BlockState) (Object) this;

		if (CommonProxy.hasDFLib && DripstoneRecipe.safeTick(state, serverLevel, blockPos, randomSource)) {
			ci.cancel();
		}
	}
}
