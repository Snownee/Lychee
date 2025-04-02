package snownee.lychee.mixin.jei;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.library.plugins.vanilla.anvil.AnvilRecipeCategory;
import snownee.lychee.compat.recipeviewer.category.AbstractRvCategory;
import snownee.lychee.compat.recipeviewer.jei.display.AnvilCraftingDisplay;
import snownee.lychee.compat.recipeviewer.jei.elements.RenderElementAdapter;

@Mixin(value = AnvilRecipeCategory.class, remap = false)
public class AnvilRecipeCategoryMixin {
	@Unique
	private final Vector2fc lychee$infoPosition = new Vector2f(83, 18);

	@Inject(method = "createRecipeExtras*", at = @At("TAIL"))
	private void onRecipeExtras(IRecipeExtrasBuilder builder, IJeiAnvilRecipe recipe, IFocusGroup focuses, CallbackInfo ci) {
		if (recipe instanceof AnvilCraftingDisplay display && AbstractRvCategory.needInfoIcon(display.recipeHolder().value())) {
			builder.addWidget(new RenderElementAdapter(AbstractRvCategory.getRecipeInfoIcon(display.recipeHolder()).at(lychee$infoPosition)));
		}
	}
}
