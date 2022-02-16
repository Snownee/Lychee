package snownee.lychee.compat.rei.display;

import java.util.List;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import snownee.lychee.block_exploding.BlockExplodingContext;
import snownee.lychee.block_exploding.BlockExplodingRecipe;
import snownee.lychee.compat.rei.REICompat;

public class BlockExplodingDisplay extends BaseREIDisplay<BlockExplodingContext, BlockExplodingRecipe> {

	public BlockExplodingDisplay(BlockExplodingRecipe recipe) {
		super(recipe);
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return REICompat.BLOCK_EXPLODING;
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		// TODO Auto-generated method stub
		return super.getInputEntries();
	}

}
