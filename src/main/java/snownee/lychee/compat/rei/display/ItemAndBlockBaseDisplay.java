package snownee.lychee.compat.rei.display;

import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;

public abstract class ItemAndBlockBaseDisplay<C extends LycheeContext, T extends LycheeRecipe<C>> extends BaseREIDisplay<C, T> {

	public ItemAndBlockBaseDisplay(T recipe) {
		super(recipe);
	}

}
