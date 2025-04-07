package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2fc;

import snownee.lychee.util.recipe.ILycheeRecipe;

public abstract class RvCategoryLayoutBuilder extends RvCategoryBuilder {
	protected RvCategoryLayoutBuilder(RvCategoryInstance<?> category) {
		super(category);
	}

	public abstract void actionGroup(ILycheeRecipe<?> recipe, Vector2fc position);

	public abstract void ingredientGroup(ILycheeRecipe<?> recipe, Vector2fc position);
}
