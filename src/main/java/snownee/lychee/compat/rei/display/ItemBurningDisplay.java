package snownee.lychee.compat.rei.display;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.item_burning.ItemBurningRecipe;

public class ItemBurningDisplay extends ItemAndBlockBaseDisplay<LycheeContext, ItemBurningRecipe> {

	public ItemBurningDisplay(ItemBurningRecipe recipe) {
		super(recipe);
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return REICompat.ITEM_BURNING;
	}

}
