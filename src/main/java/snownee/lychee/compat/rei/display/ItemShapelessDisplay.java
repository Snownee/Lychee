package snownee.lychee.compat.rei.display;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.core.recipe.ItemShapelessRecipe;

public class ItemShapelessDisplay<C extends ItemShapelessContext, T extends ItemShapelessRecipe<C>> extends BaseREIDisplay<C, T> {

	public ItemShapelessDisplay(T recipe) {
		super(recipe);
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return CategoryIdentifier.of(recipe.getType().id);
	}

}
