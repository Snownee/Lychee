package snownee.lychee.compat.rei.display;

import java.util.List;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.recipe.ItemAndBlockRecipe;

public class ItemInsideDisplay<T extends ItemAndBlockRecipe<LycheeContext>> extends ItemAndBlockBaseDisplay<LycheeContext, T> {

	public ItemInsideDisplay(T recipe) {
		super(recipe);
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		return List.of(EntryIngredients.ofIngredient(recipe.getInput()), EntryIngredients.ofItemStacks(BlockPredicateHelper.getMatchedItemStacks(recipe.getBlock())));
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return REICompat.ITEM_INSIDE;
	}

}
