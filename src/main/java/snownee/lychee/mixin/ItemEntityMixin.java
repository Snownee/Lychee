package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import snownee.lychee.LycheeTags;
import snownee.lychee.RecipeTypes;
import snownee.lychee.item_burning.ItemBurningRecipe;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

	@Inject(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V"
			), method = "hurt"
	)
	private void lychee_hurt(DamageSource pSource, float pAmount, CallbackInfoReturnable<Boolean> ci) {
		if (RecipeTypes.ITEM_BURNING.isEmpty()) {
			return;
		}
		Entity entity = (Entity) (Object) this;
		if (entity.isAlive() && entity.getType() == EntityType.ITEM && !entity.level.isClientSide && entity.isOnFire()) {
			ItemBurningRecipe.on((ItemEntity) entity);
		}
	}

	@Inject(at = @At("HEAD"), method = "fireImmune", cancellable = true)
	private void lychee_fireImmune(CallbackInfoReturnable<Boolean> ci) {
		if (((ItemEntity) (Object) this).getItem().is(LycheeTags.FIRE_IMMUNE)) {
			ci.setReturnValue(true);
		}
	}
}
