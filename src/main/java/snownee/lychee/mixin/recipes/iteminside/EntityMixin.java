package snownee.lychee.mixin.recipes.iteminside;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import snownee.lychee.RecipeTypes;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(at = @At("HEAD"), method = "tick")
	private void lychee_tick(CallbackInfo ci) {
		if (RecipeTypes.ITEM_INSIDE.isEmpty()) {
			return;
		}
		final var entity = (Entity) (Object) this;
		if (!entity.isAlive() || entity.getType() != EntityType.ITEM || entity.level().isClientSide || entity.tickCount % 20 != 10) {
			return;
		}

		RecipeTypes.ITEM_INSIDE.process(entity, ((ItemEntity) entity).getItem(), entity.blockPosition(), entity.position());
	}
}
