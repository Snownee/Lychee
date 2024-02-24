package snownee.lychee.mixin.recipes.itemburing;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import snownee.lychee.RecipeTypes;
import snownee.lychee.recipes.ItemBurningRecipe;

@Mixin(ItemEntity.class)
public class ItemEntityHurtMixin {

	@Inject(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V",
					shift = At.Shift.AFTER
			), method = "hurt"
	)
	private void lychee_hurt(DamageSource pSource, float pAmount, CallbackInfoReturnable<Boolean> ci) {
		if (RecipeTypes.ITEM_BURNING.isEmpty()) {
			return;
		}
		Entity entity = (Entity) (Object) this;
		if (!entity.isAlive() && entity.getType() == EntityType.ITEM && entity.isOnFire()) {
			ItemBurningRecipe.invoke((ItemEntity) entity);
		}
	}

}
