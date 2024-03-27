package snownee.lychee.util.particles.dripstone;

import java.util.concurrent.ExecutionException;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.loader.Platform;
import snownee.lychee.Lychee;
import snownee.lychee.RecipeTypes;
import snownee.lychee.mixin.particles.PointedDripstoneBlockAccess;

public class DripstoneParticleService {

	public static final Cache<Block, DripParticleHandler> particleHandlers = CacheBuilder.newBuilder().build();

	public static final ParticleType<BlockParticleOption> DRIPSTONE_DRIPPING = FabricParticleTypes.complex(
			BlockParticleOption.DESERIALIZER,
			BlockParticleOption::codec,
			BlockParticleOption::streamCodec);
	public static final ParticleType<BlockParticleOption> DRIPSTONE_FALLING = FabricParticleTypes.complex(
			BlockParticleOption.DESERIALIZER,
			BlockParticleOption::codec,
			BlockParticleOption::streamCodec);
	public static final ParticleType<BlockParticleOption> DRIPSTONE_SPLASH = FabricParticleTypes.complex(
			BlockParticleOption.DESERIALIZER,
			BlockParticleOption::codec,
			BlockParticleOption::streamCodec);

	public static boolean spawnDripParticle(Level level, BlockPos blockPos, BlockState blockState) {
		var sourceBlock = findBlockAboveStalactite(level, blockPos, blockState);
		if (sourceBlock == null || !RecipeTypes.DRIPSTONE_DRIPPING.hasSource(sourceBlock.getBlock())) {
			return false;
		}
		var sourceFluid = sourceBlock.getFluidState();
		if (sourceFluid.is(FluidTags.LAVA) || sourceFluid.is(FluidTags.WATER)) {
			return false;
		}
		var handler = getParticleHandler(level, sourceBlock);
		if (handler == null) {
			return false;
		}
		var vec3 = blockState.getOffset(level, blockPos);
		var e = (double) blockPos.getX() + 0.5 + vec3.x;
		var f = (double) ((float) (blockPos.getY() + 1) - 0.6875f) - 0.0625;
		var g = (double) blockPos.getZ() + 0.5 + vec3.z;
		handler.addParticle(level, blockPos, sourceBlock, e, f, g);
		return true;
	}

	public static DripParticleHandler getParticleHandler(Level level, BlockState sourceBlock) {
		var block = sourceBlock.getBlock();
		try {
			return particleHandlers.get(block, () -> {
				if (!Platform.isPhysicalClient()) {
					return DripParticleHandler.SIMPLE_DUMMY;
				}
				var defaultState = block.defaultBlockState();
				var color = defaultState.getMapColor(level, BlockPos.ZERO).col;
				return new DripParticleHandler.Simple(color, defaultState.getLightEmission() > 4);
			});
		} catch (ExecutionException e) {
			Lychee.LOGGER.error("", e);
		}
		return null;
	}

	@Nullable
	public static BlockState findBlockAboveStalactite(Level level, BlockPos pos, BlockState state) {
		return PointedDripstoneBlockAccess
				.callFindRootBlock(level, pos, state, 11)
				.map(blockPos -> level.getBlockState(blockPos.above()))
				.orElse(null);
	}
}
