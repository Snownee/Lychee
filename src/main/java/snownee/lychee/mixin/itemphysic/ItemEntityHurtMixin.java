package snownee.lychee.mixin.itemphysic;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import snownee.lychee.RecipeTypes;
import snownee.lychee.item_burning.ItemBurningRecipe;
import team.creative.itemphysic.server.ItemPhysicServer;

@Mixin(ItemPhysicServer.class)
public class ItemEntityHurtMixin {

	@Inject(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;discard()V", shift = At.Shift.AFTER, remap = true
			), method = "hurt", remap = false
	)
	private static void lychee_hurt(ItemEntity entity, DamageSource pSource, float pAmount, CallbackInfoReturnable<Boolean> ci) {
		if (RecipeTypes.ITEM_BURNING.isEmpty()) {
			return;
		}
		if (!entity.isAlive() && entity.getType() == EntityType.ITEM && entity.isOnFire()) {
			ItemBurningRecipe.on(entity);
		}
	}

}
