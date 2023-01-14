package snownee.lychee.compat.rei.display;

import java.util.List;

import com.google.common.collect.Lists;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import snownee.lychee.block_crushing.BlockCrushingRecipe;

public class BlockCrushingDisplay extends BaseREIDisplay<BlockCrushingRecipe> {

	public BlockCrushingDisplay(BlockCrushingRecipe recipe, CategoryIdentifier<?> categoryId) {
		super(recipe, categoryId);
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		List<EntryIngredient> items = Lists.newArrayList(super.getInputEntries());
		addBlockInputs(items, recipe.getBlock());
		addBlockInputs(items, recipe.getLandingBlock());
		return items;
	}

}
