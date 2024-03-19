package snownee.lychee.mixin.particles;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.recipes.DripstoneRecipe;
import snownee.lychee.util.particles.dripstone.DripstoneParticleService;

@Mixin(value = PointedDripstoneBlock.class, priority = 1100)
public class PointedDripstoneBlockMixin {

	@Inject(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/PointedDripstoneBlock;getFluidAboveStalactite" +
							"(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;" +
							"Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;"
			),
			method = "maybeTransferFluid",
			cancellable = true
	)
	private static void lychee_maybeTransferFluid(
			BlockState blockState,
			ServerLevel serverLevel,
			BlockPos blockPos,
			float f,
			CallbackInfo ci
	) {
		if (DripstoneRecipe.invoke(blockState, serverLevel, blockPos)) {
			ci.cancel();
		}
	}

	@Inject(
			at = @At(
					"HEAD"
			),
			method = "spawnDripParticle(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;" +
					"Lnet/minecraft/world/level/block/state/BlockState;)V",
			cancellable = true
	)
	private static void lychee_spawnDripParticle(
			Level level,
			BlockPos blockPos,
			BlockState blockState,
			CallbackInfo ci
	) {
		if (DripstoneParticleService.spawnDripParticle(level, blockPos, blockState)) {
			ci.cancel();
		}
	}

	@Inject(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/PointedDripstoneBlock;getFluidAboveStalactite" +
							"(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;" +
							"Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;"
			), method = "animateTick", cancellable = true
	)
	private void animateTick(
			BlockState blockState,
			Level level,
			BlockPos blockPos,
			RandomSource randomSource,
			CallbackInfo ci
	) {
		if (DripstoneParticleService.spawnDripParticle(level, blockPos, blockState)) {
			ci.cancel();
		}
	}
}
