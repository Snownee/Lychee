package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.tags.TagContainer;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.recipe.type.LycheeRecipeType;

@Mixin(TagContainer.class)
public class TagContainerMixin {

	// because tags are loaded after recipes, we bake cache here
	@Inject(at = @At("TAIL"), method = "bindToGlobal")
	private void lychee_bindToGlobal(CallbackInfo ci) {
		RecipeTypes.ALL.forEach(LycheeRecipeType::buildCache);
		RecipeTypes.ALL.forEach(LycheeRecipeType::updateEmptyState);
	}

}
