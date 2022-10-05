package snownee.lychee.compat.rei.display;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.item_inside.ItemInsideRecipe;

public class ItemInsideDisplay extends ItemAndBlockBaseDisplay<ItemInsideRecipe> {

	public ItemInsideDisplay(ItemInsideRecipe recipe) {
		super(recipe);
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return REICompat.ITEM_INSIDE;
	}

}
