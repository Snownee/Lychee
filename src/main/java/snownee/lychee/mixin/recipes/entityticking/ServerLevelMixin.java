package snownee.lychee.mixin.recipes.entityticking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.LycheeEntityType;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
	@Inject(method = "tickNonPassenger", at = @At("RETURN"))
	public void lychee_tick(Entity entity, CallbackInfo ci) {
		RecipeTypes.ENTITY_TICKING.process(entity, ((LycheeEntityType) entity.getType()).lychee$tickingRecipes());
	}
}
