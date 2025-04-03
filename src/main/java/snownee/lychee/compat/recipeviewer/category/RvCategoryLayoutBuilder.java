package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2fc;

import snownee.lychee.util.recipe.ILycheeRecipe;

public interface RvCategoryLayoutBuilder {
	void actionGroup(ILycheeRecipe<?> recipe, Vector2fc position);

	void ingredientGroup(ILycheeRecipe<?> recipe, Vector2fc position);
}
