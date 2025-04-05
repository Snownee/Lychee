package snownee.lychee.mixin.recipes.entityticking;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.LycheeEntity;
import snownee.lychee.util.LycheeEntityType;
import snownee.lychee.util.context.LycheeContext;

@Mixin(Entity.class)
public class EntityMixin implements LycheeEntity {
	@Unique
	private @Nullable LycheeContext lychee$context;

	@Inject(method = "rideTick", at = @At("RETURN"))
	public void lychee_rideTick(CallbackInfo ci) {
		Entity entity = (Entity) (Object) this;
		RecipeTypes.ENTITY_TICKING.process(entity, ((LycheeEntityType) entity.getType()).lychee$tickingRecipes());
	}

	@Override
	public LycheeContext lychee$getContext() {
		return lychee$context;
	}

	@Override
	public void lychee$setContext(LycheeContext context) {
		lychee$context = context;
	}
}
