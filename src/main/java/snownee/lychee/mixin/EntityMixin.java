package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.TypedInstance;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import snownee.lychee.LycheeTags;

@Mixin(Entity.class)
public abstract class EntityMixin implements TypedInstance<EntityType<?>> {

	@Shadow
	public abstract EntityType<?> getType();

	@Shadow
	public abstract void clearFire();

	@Inject(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"),
			method = "thunderHit",
			cancellable = true)
	private void lychee_thunderHit_hurt(ServerLevel level, LightningBolt lightningBolt, CallbackInfo ci) {
		if (is(LycheeTags.LIGHTNING_IMMUNE)) {
			ci.cancel();
		}

		if (is(LycheeTags.LIGHTING_FIRE_IMMUNE)) {
			clearFire();
		}
	}
}
