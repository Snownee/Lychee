package snownee.lychee.compat.rei.display;

import java.util.List;

import com.google.common.collect.Lists;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.block_crushing.BlockCrushingRecipe;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.core.def.BlockPredicateHelper;

public class BlockCrushingDisplay extends BaseREIDisplay<BlockCrushingRecipe> {

	public BlockCrushingDisplay(BlockCrushingRecipe recipe) {
		super(recipe);
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return REICompat.BLOCK_CRUSHING;
	}

	@Override
	public List<EntryIngredient> getInputEntries() {
		List<EntryIngredient> items = Lists.newArrayList(super.getInputEntries());
		addBlockInputs(items, recipe.getBlock());
		addBlockInputs(items, recipe.getLandingBlock());
		return items;
	}

}
