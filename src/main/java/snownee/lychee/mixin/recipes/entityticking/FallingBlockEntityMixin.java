package snownee.lychee.mixin.recipes.entityticking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.LycheeEntityType;

@Mixin(FallingBlockEntity.class)
public class FallingBlockEntityMixin {
	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/FallingBlockEntity;handlePortal()V"))
	public void lychee_tick(CallbackInfo ci) {
		Entity entity = (Entity) (Object) this;
		RecipeTypes.ENTITY_TICKING.process(entity, ((LycheeEntityType) entity.getType()).lychee$tickingRecipes());
	}
}
