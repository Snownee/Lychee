package snownee.lychee.mixin.recipes.entityticking;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import snownee.lychee.recipes.EntityTickingRecipe;
import snownee.lychee.util.LycheeEntityType;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;

@Mixin(Entity.class)
public class EntityMixin {
	@Unique
	private LycheeContext lychee$context;

	@Inject(method = "baseTick", at = @At("HEAD"))
	public void lychee_baseTick(CallbackInfo ci) {
		Entity entity = (Entity) (Object) this;
		if (entity.level().isClientSide) {
			return;
		}
		List<EntityTickingRecipe> recipes = ((LycheeEntityType) entity.getType()).lychee$tickingRecipes();
		if (recipes.isEmpty()) {
			return;
		}
		LycheeContext context = lychee$context;
		if (context == null) {
			context = new LycheeContext();
			context.put(LycheeContextKey.LEVEL, entity.level());
			lychee$context = context;
		}
		for (EntityTickingRecipe recipe : recipes) {
			if (recipe.matches(context, entity.level())) {

			}
		}
	}
}
