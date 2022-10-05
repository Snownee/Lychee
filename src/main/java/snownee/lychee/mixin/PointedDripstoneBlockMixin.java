package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.dripstone_dripping.DripstoneRecipe;

@Mixin(PointedDripstoneBlock.class)
public class PointedDripstoneBlockMixin {

	@Inject(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/world/level/block/PointedDripstoneBlock;getFluidAboveStalactite(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;"
			), method = "maybeTransferFluid", cancellable = true
	)
	private static void lychee_maybeTransferFluid(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, float f, CallbackInfo ci) {
		if (DripstoneRecipe.on(blockState, serverLevel, blockPos, f))
			ci.cancel();
	}

}
