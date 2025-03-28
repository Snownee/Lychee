package snownee.lychee.compat.recipeviewer.jei.category;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.compat.recipeviewer.RvCategory;
import snownee.lychee.recipes.ItemBurningRecipe;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

@NotNullByDefault
public class ItemBurningRecipeCategory extends ItemAndBlockBaseCategory<ItemBurningRecipe> {

	public ItemBurningRecipeCategory(RvCategory<ItemBurningRecipe> category) {
		super(category);
		methodRect.setX(27);
	}

	@Override
	public BlockPredicate getInputBlock(ItemBurningRecipe recipe) {
		return BlockPredicateExtensions.ANY;
	}

	@Override
	public BlockState getRenderingBlock(ItemBurningRecipe recipe) {
		return Blocks.FIRE.defaultBlockState();
	}

	@Override
	protected boolean needRenderInputBlock(ItemBurningRecipe recipe) {
		return false;
	}
}
