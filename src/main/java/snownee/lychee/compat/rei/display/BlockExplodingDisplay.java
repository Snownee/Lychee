package snownee.lychee.compat.rei.display;

import java.util.List;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import snownee.lychee.block_exploding.BlockExplodingRecipe;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.core.def.BlockPredicateHelper;

public class BlockExplodingDisplay extends BaseREIDisplay<BlockExplodingRecipe> {

	public BlockExplodingDisplay(BlockExplodingRecipe recipe) {
		super(recipe);
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return REICompat.BLOCK_EXPLODING;
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		return List.of(EntryIngredients.ofItemStacks(BlockPredicateHelper.getMatchedItemStacks(recipe.getBlock())));
	}

}
