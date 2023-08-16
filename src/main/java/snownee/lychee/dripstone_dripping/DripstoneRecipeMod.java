package snownee.lychee.dripstone_dripping;

import java.util.concurrent.ExecutionException;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.Lychee;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.CommonProxy;

public class DripstoneRecipeMod {

	public static final Cache<Block, DripParticleHandler> particleHandlers = CacheBuilder.newBuilder().build();

	public static final ParticleType<BlockParticleOption> DRIPSTONE_DRIPPING = CommonProxy.registerParticleType(BlockParticleOption.DESERIALIZER);
	public static final ParticleType<BlockParticleOption> DRIPSTONE_FALLING = CommonProxy.registerParticleType(BlockParticleOption.DESERIALIZER);
	public static final ParticleType<BlockParticleOption> DRIPSTONE_SPLASH = CommonProxy.registerParticleType(BlockParticleOption.DESERIALIZER);

	public static boolean spawnDripParticle(Level level, BlockPos blockPos, BlockState blockState) {
		BlockState sourceBlock = DripstoneRecipe.getBlockAboveStalactite(level, blockPos, blockState);
		if (sourceBlock == null || !RecipeTypes.DRIPSTONE_DRIPPING.hasSource(sourceBlock.getBlock())) {
			return false;
		}
		FluidState sourceFluid = sourceBlock.getFluidState();
		if (sourceFluid.is(FluidTags.LAVA) || sourceFluid.is(FluidTags.WATER)) {
			return false;
		}
		if (CommonProxy.hasModdedDripParticle(sourceFluid)) {
			return false;
		}
		DripParticleHandler handler = getParticleHandler(level, sourceBlock);
		if (handler == null) {
			return false;
		}
		Vec3 vec3 = blockState.getOffset(level, blockPos);
		double e = (double) blockPos.getX() + 0.5 + vec3.x;
		double f = (double) ((float) (blockPos.getY() + 1) - 0.6875f) - 0.0625;
		double g = (double) blockPos.getZ() + 0.5 + vec3.z;
		handler.addParticle(level, blockPos, sourceBlock, e, f, g);
		return true;
	}

	public static DripParticleHandler getParticleHandler(Level level, BlockState sourceBlock) {
		Block block = sourceBlock.getBlock();
		try {
			return particleHandlers.get(block, () -> {
				if (!CommonProxy.isPhysicalClient()) {
					return DripParticleHandler.SIMPLE_DUMMY;
				}
				BlockState defaultState = block.defaultBlockState();
				int color = defaultState.getMapColor(level, BlockPos.ZERO).col;
				return new DripParticleHandler.Simple(color, defaultState.getLightEmission() > 4);
			});
		} catch (ExecutionException e) {
			Lychee.LOGGER.error("", e);
		}
		return null;
	}

}
