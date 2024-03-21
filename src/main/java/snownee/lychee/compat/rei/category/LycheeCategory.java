package snownee.lychee.compat.rei.category;

import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public interface LycheeCategory<R extends ILycheeRecipe<LycheeContext>> {
	int WIDTH = 150;
	int HEIGHT = 59;

	LycheeRecipeType<?, ? extends R> recipeType();

	default int contentWidth() {
		return 120;
	}
}
