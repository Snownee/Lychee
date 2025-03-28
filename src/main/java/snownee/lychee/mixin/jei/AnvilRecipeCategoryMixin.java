package snownee.lychee.mixin.jei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.library.plugins.vanilla.anvil.AnvilRecipeCategory;
import net.minecraft.client.renderer.Rect2i;
import snownee.lychee.compat.recipeviewer.jei.category.LycheeCategory;
import snownee.lychee.compat.recipeviewer.jei.display.AnvilCraftingDisplay;

@Mixin(value = AnvilRecipeCategory.class, remap = false)
public class AnvilRecipeCategoryMixin {
	@Unique
	private Rect2i infoRect = new Rect2i(83, 18, 8, 8);

	@Inject(method = "createRecipeExtras", at = @At("TAIL"))
	private void onRecipeExtras(IRecipeExtrasBuilder builder, IJeiAnvilRecipe recipe, IFocusGroup focuses, CallbackInfo ci) {
		if (recipe instanceof AnvilCraftingDisplay display) {
			LycheeCategory.createInfoBadgeIfNeeded(builder, display.recipeHolder(), infoRect);
		}
	}
}
