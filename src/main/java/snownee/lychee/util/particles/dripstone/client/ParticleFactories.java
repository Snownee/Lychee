package snownee.lychee.util.particles.dripstone.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import snownee.lychee.util.particles.dripstone.DripParticleHandler;
import snownee.lychee.util.particles.dripstone.DripstoneParticleService;
import snownee.lychee.util.particles.dripstone.DripstoneSplashParticle;

//Modified from Dripstone Fluid Lib
public class ParticleFactories {
	private static void postParticle(
			DripParticle particle,
			BlockState blockState,
			ClientLevel level,
			double x,
			double y,
			double z,
			double velocityX,
			double velocityY,
			double velocityZ
	) {
		DripParticleHandler handler = DripstoneParticleService.getParticleHandler(level, blockState);
		if (handler == null) {
			return;
		}
		int color = handler.getColor(level, blockState, x, y, z, velocityX, velocityY, velocityZ);
		float r = (color >> 16 & 255) / 255f;
		float g = (color >> 8 & 255) / 255f;
		float b = (color & 255) / 255f;
		particle.setColor(r, g, b);
		particle.isGlowing = handler.isGlowing(level, blockState);
	}

	public static class Dripping implements ParticleProvider<BlockParticleOption> {
		protected final SpriteSet sprite;

		public Dripping(SpriteSet sprite) {
			this.sprite = sprite;
		}

		@Override
		public Particle createParticle(
				BlockParticleOption defaultParticleType,
				ClientLevel level,
				double x,
				double y,
				double z,
				double velocityX,
				double velocityY,
				double velocityZ
		) {
			BlockParticleOption fallOption = new BlockParticleOption(
					DripstoneParticleService.DRIPSTONE_FALLING,
					defaultParticleType.getState()
			);
			DripParticle particle = new DripParticle.DripHangParticle(level, x, y, z, Fluids.WATER, fallOption);
			particle.pickSprite(this.sprite);
			postParticle(particle, defaultParticleType.getState(), level, x, y, z, velocityX, velocityY, velocityZ);
			return particle;
		}
	}

	public static class Falling implements ParticleProvider<BlockParticleOption> {
		protected final SpriteSet sprite;

		public Falling(SpriteSet sprite) {
			this.sprite = sprite;
		}

		@Override
		public Particle createParticle(
				BlockParticleOption defaultParticleType,
				ClientLevel level,
				double x,
				double y,
				double z,
				double velocityX,
				double velocityY,
				double velocityZ
		) {
			BlockParticleOption fallOption = new BlockParticleOption(
					DripstoneParticleService.DRIPSTONE_SPLASH,
					defaultParticleType.getState()
			);
			DripParticle particle = new DripParticle.DripstoneFallAndLandParticle(
					level,
					x,
					y,
					z,
					Fluids.WATER,
					fallOption
			);
			particle.pickSprite(this.sprite);
			postParticle(particle, defaultParticleType.getState(), level, x, y, z, velocityX, velocityY, velocityZ);
			return particle;
		}
	}

	public static class Splash implements ParticleProvider<BlockParticleOption> {
		protected final SpriteSet sprite;

		public Splash(SpriteSet sprite) {
			this.sprite = sprite;
		}

		@Override
		public Particle createParticle(
				BlockParticleOption defaultParticleType,
				ClientLevel level,
				double x,
				double y,
				double z,
				double velocityX,
				double velocityY,
				double velocityZ
		) {
			DripstoneSplashParticle particle = new DripstoneSplashParticle(
					level,
					x,
					y,
					z,
					velocityX,
					velocityY,
					velocityZ,
					Fluids.WATER
			);
			particle.pickSprite(this.sprite);
			postParticle(particle, defaultParticleType.getState(), level, x, y, z, velocityX, velocityY, velocityZ);
			return particle;
		}
	}

}
