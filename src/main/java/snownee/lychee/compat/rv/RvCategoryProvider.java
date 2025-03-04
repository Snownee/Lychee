package snownee.lychee.compat.rv;

import snownee.lychee.util.recipe.ILycheeRecipe;

public interface RvCategoryProvider<T extends ILycheeRecipe<?>> {
	RvCategory<T> rvCategory();
}
