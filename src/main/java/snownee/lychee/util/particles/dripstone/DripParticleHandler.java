package snownee.lychee.util.particles.dripstone;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface DripParticleHandler {

	Simple SIMPLE_DUMMY = new Simple(0, false);

	record Simple(int color, boolean glowing) implements DripParticleHandler {

		@Override
		public void addParticle(Level level, BlockPos blockPos, BlockState blockState, double x, double y, double z) {
			level.addParticle(
					new BlockParticleOption(DripstoneParticleService.DRIPSTONE_DRIPPING, blockState),
					x,
					y,
					z,
					0,
					0,
					0
			);
		}

		@Override
		public int getColor(
				ClientLevel level,
				BlockState blockState,
				double x,
				double y,
				double z,
				double velocityX,
				double velocityY,
				double velocityZ
		) {
			return color;
		}

		@Override
		public boolean isGlowing(ClientLevel level, BlockState blockState) {
			return glowing;
		}

	}

	void addParticle(Level level, BlockPos blockPos, BlockState blockState, double x, double y, double z);

	int getColor(
			ClientLevel level,
			BlockState blockState,
			double x,
			double y,
			double z,
			double velocityX,
			double velocityY,
			double velocityZ
	);

	boolean isGlowing(ClientLevel level, BlockState blockState);

}
