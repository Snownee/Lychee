package snownee.lychee.compat.rei.display;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.interaction.BlockInteractingRecipe;

public class BlockInteractionDisplay extends ItemInsideDisplay<BlockInteractingRecipe> {

	public BlockInteractionDisplay(BlockInteractingRecipe recipe) {
		super(recipe);
	}

	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return REICompat.BLOCK_INTERACTION;
	}

}
