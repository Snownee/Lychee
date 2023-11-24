package snownee.lychee.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import snownee.lychee.LycheeTags;
import snownee.lychee.RecipeTypes;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Shadow
	public abstract EntityType<?> getType();

	@Shadow
	public abstract void setRemainingFireTicks(int i);

	@Inject(at = @At("HEAD"), method = "tick")
	private void lychee_tick(CallbackInfo ci) {
		if (RecipeTypes.ITEM_INSIDE.isEmpty()) {
			return;
		}
		Entity entity = (Entity) (Object) this;
		if (entity.isAlive() && entity.getType() == EntityType.ITEM && !entity.level.isClientSide && entity.tickCount % 20 == 10) {
			RecipeTypes.ITEM_INSIDE.process(entity, ((ItemEntity) entity).getItem(), entity.blockPosition(), entity.position());
		}
	}

	@Inject(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
					shift = At.Shift.BEFORE
			), method = "thunderHit", cancellable = true
	)
	private void lychee_thunderHit_hurt(ServerLevel serverLevel, LightningBolt lightningBolt, CallbackInfo ci) {
		if (getType().is(LycheeTags.LIGHTNING_IMMUNE)) {
			ci.cancel();
		}

		if (getType().is(LycheeTags.LIGHTING_FIRE_IMMUNE)) {
			setRemainingFireTicks(0);
		}
	}
}
