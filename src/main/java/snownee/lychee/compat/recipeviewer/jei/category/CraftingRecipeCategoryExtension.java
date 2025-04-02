package snownee.lychee.compat.recipeviewer.jei.category;

import java.util.List;
import java.util.Objects;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.compat.recipeviewer.category.AbstractRvCategory;
import snownee.lychee.compat.recipeviewer.jei.elements.RenderElementAdapter;
import snownee.lychee.recipes.ShapedCraftingRecipe;

@NotNullByDefault
public class CraftingRecipeCategoryExtension implements ICraftingCategoryExtension<ShapedCraftingRecipe> {

	private static final Vector2fc INFO_POSITION = new Vector2f(67, 11);

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
		if (AbstractRvCategory.needInfoIcon(recipeHolder.value())) {
			builder.addWidget(new RenderElementAdapter(AbstractRvCategory.getRecipeInfoIcon(recipeHolder).at(INFO_POSITION)));
		}
	}

	@Override
	public void setRecipe(
			RecipeHolder<ShapedCraftingRecipe> recipeHolder,
			IRecipeLayoutBuilder builder,
			ICraftingGridHelper craftingGridHelper,
			IFocusGroup focuses) {
		var craftingRecipe = recipeHolder.value();
		var inputs = craftingRecipe.getIngredients().stream().map(ingredient -> List.of(ingredient.getItems())).toList();
		var resultItem = craftingRecipe.getResultItem(Objects.requireNonNull(Minecraft.getInstance().level).registryAccess());

		var width = getWidth(recipeHolder);
		var height = getHeight(recipeHolder);
		craftingGridHelper.createAndSetOutputs(builder, List.of(resultItem));
		craftingGridHelper.createAndSetInputs(builder, inputs, width, height);
	}
}