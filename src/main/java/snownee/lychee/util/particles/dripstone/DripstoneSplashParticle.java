package snownee.lychee.util.particles.dripstone;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.world.level.material.Fluid;

// Modified from Dripstone Fluid Lib
// SplashParticle
public class DripstoneSplashParticle extends DripParticle {
	public DripstoneSplashParticle(
			ClientLevel clientWorld,
			double d,
			double e,
			double f,
			double g,
			double h,
			double i,
			Fluid fluid
	) {
		super(clientWorld, d, e, f, fluid);
		this.xd *= 0.3f;
		this.yd = Math.random() * (double) 0.2f + (double) 0.1f;
		this.zd *= 0.3f;
		this.gravity = 0.04f;
		if (h == 0.0 && (g != 0.0 || i != 0.0)) {
			this.xd = g;
			this.yd = 0.1;
			this.zd = i;
		}
		this.lifetime = (int) (8.0 / (Math.random() * 0.8 + 0.2));
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.lifetime-- <= 0) {
			this.remove();
		} else {
			this.yd -= this.gravity;
			this.move(this.xd, this.yd, this.zd);
			this.xd *= 0.98F;
			this.yd *= 0.98F;
			this.zd *= 0.98F;
			if (this.onGround) {
				if (Math.random() < 0.5) {
					this.remove();
				}

				this.xd *= 0.7F;
				this.zd *= 0.7F;
			}
		}
	}
}
