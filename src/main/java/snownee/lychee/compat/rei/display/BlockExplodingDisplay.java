package snownee.lychee.compat.rei.display;

import java.util.List;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import snownee.lychee.block_exploding.BlockExplodingRecipe;
import snownee.lychee.core.def.BlockPredicateHelper;

public class BlockExplodingDisplay extends BaseREIDisplay<BlockExplodingRecipe> {

	public BlockExplodingDisplay(BlockExplodingRecipe recipe, CategoryIdentifier<?> categoryId) {
		super(recipe, categoryId);
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		return List.of(EntryIngredients.ofItemStacks(BlockPredicateHelper.getMatchedItemStacks(recipe.getBlock())));
	}

}
