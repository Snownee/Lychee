package snownee.lychee.compat.rei.display;

import snownee.lychee.core.recipe.LycheeRecipe;

public abstract class ItemAndBlockBaseDisplay<T extends LycheeRecipe<?>> extends BaseREIDisplay<T> {

	public ItemAndBlockBaseDisplay(T recipe) {
		super(recipe);
	}

}
