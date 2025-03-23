package snownee.lychee.compat.rv.category;

import org.joml.Vector2ic;

import snownee.lychee.util.recipe.ILycheeRecipe;

public interface RvCategoryLayoutBuilder {
	void actionGroup(ILycheeRecipe<?> recipe, Vector2ic position);

	void ingredientGroup(ILycheeRecipe<?> recipe, Vector2ic position);
}
