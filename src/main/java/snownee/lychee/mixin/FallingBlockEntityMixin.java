package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import snownee.lychee.RecipeTypes;

@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityMixin {

	@Inject(at = @At("HEAD"), method = "causeFallDamage")
	private void lychee_causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource, CallbackInfoReturnable<Boolean> ci) {
		FallingBlockEntity entity = (FallingBlockEntity) (Object) this;
		if (entity.level.isClientSide) {
			return;
		}
		RecipeTypes.BLOCK_CRUSHING.process(entity, entity.blockPosition(), entity.position());
	}

}
