package snownee.lychee.compat.rei.display;

import java.util.List;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.item_burning.ItemBurningRecipe;

public class ItemBurningDisplay extends ItemAndBlockBaseDisplay<LycheeContext, ItemBurningRecipe> {

	public ItemBurningDisplay(ItemBurningRecipe recipe) {
		super(recipe);
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		return List.of(EntryIngredients.ofIngredient(recipe.getInput()));
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return REICompat.ITEM_BURNING;
	}

}
