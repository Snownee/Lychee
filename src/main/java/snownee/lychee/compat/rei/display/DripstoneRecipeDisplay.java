package snownee.lychee.compat.rei.display;

import java.util.List;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.dripstone_dripping.DripstoneRecipe;

public class DripstoneRecipeDisplay extends BaseREIDisplay<DripstoneRecipe> {

	public DripstoneRecipeDisplay(DripstoneRecipe recipe) {
		super(recipe);
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return REICompat.DRIPSTONE_DRIPPING;
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		return List.of(EntryIngredients.ofItemStacks(BlockPredicateHelper.getMatchedItemStacks(recipe.getSourceBlock())), EntryIngredients.ofItemStacks(BlockPredicateHelper.getMatchedItemStacks(recipe.getBlock())));
	}

}