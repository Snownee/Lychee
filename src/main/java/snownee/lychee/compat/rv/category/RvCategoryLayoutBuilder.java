package snownee.lychee.compat.rv.category;

import org.joml.Vector2i;

import snownee.lychee.util.recipe.ILycheeRecipe;

public interface RvCategoryLayoutBuilder {
	void actionGroup(ILycheeRecipe<?> recipe, Vector2i position);

	void ingredientGroup(ILycheeRecipe<?> recipe, Vector2i position);
}
