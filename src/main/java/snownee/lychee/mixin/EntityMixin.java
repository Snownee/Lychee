package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import snownee.lychee.LycheeTags;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow
	public abstract EntityType<?> getType();

	@Shadow
	public abstract void clearFire();

	@Inject(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;" +
							"F)Z",
					shift = At.Shift.BEFORE
			), method = "thunderHit", cancellable = true
	)
	private void lychee_thunderHit_hurt(ServerLevel serverLevel, LightningBolt lightningBolt, CallbackInfo ci) {
		if (getType().is(LycheeTags.LIGHTNING_IMMUNE)) {
			ci.cancel();
		}

		if (getType().is(LycheeTags.LIGHTING_FIRE_IMMUNE)) {
			clearFire();
		}
	}
}
