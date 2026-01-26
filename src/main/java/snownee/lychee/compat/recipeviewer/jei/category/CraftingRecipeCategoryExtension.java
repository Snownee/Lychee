package snownee.lychee.compat.recipeviewer.jei.category;

import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import snownee.lychee.compat.recipeviewer.category.RvCategory;
import snownee.lychee.compat.recipeviewer.jei.element.RenderElementAdapter;
import snownee.lychee.recipes.ShapedCraftingRecipe;
import snownee.lychee.util.Displays;


public class CraftingRecipeCategoryExtension implements ICraftingCategoryExtension<ShapedCraftingRecipe> {

	private static final Vector2fc INFO_POSITION = new Vector2f(67, 11);

	@Override
	public List<SlotDisplay> getIngredients(RecipeHolder<ShapedCraftingRecipe> recipeHolder) {
		return recipeHolder.value().getIngredients().stream().map(Ingredient::display).toList();
	}

	@Override
	public int getWidth(RecipeHolder<ShapedCraftingRecipe> recipeHolder) {
		return recipeHolder.value().getWidth();
	}

	@Override
	public int getHeight(RecipeHolder<ShapedCraftingRecipe> recipeHolder) {
		return recipeHolder.value().getHeight();
	}

	@Override
	public void createRecipeExtras(
			RecipeHolder<ShapedCraftingRecipe> recipeHolder,
			IRecipeExtrasBuilder builder,
			ICraftingGridHelper craftingGridHelper,
			IFocusGroup focuses) {
		if (RvCategory.needInfo(recipeHolder.value())) {
			builder.addWidget(new RenderElementAdapter(RvCategory.infoIcon(recipeHolder).at(INFO_POSITION)));
		}
	}

	@Override
	public void setRecipe(
			RecipeHolder<ShapedCraftingRecipe> recipeHolder,
			IRecipeLayoutBuilder builder,
			ICraftingGridHelper craftingGridHelper,
			IFocusGroup focuses) {
		var craftingRecipe = recipeHolder.value();
		var width = getWidth(recipeHolder);
		var height = getHeight(recipeHolder);
		craftingGridHelper.createAndSetIngredients(builder, craftingRecipe.getIngredients(), width, height);
		craftingGridHelper.createAndSetOutputs(builder, Displays.slot(craftingRecipe.result()));
	}
}