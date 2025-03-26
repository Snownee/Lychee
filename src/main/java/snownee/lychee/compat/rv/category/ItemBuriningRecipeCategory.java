package snownee.lychee.compat.rv.category;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.recipes.BlockInteractingRecipe;
import snownee.lychee.util.VectorExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;

public class ItemBuriningRecipeCategory extends ItemAndBlockCategory<BlockInteractingRecipe> {
	public ItemBuriningRecipeCategory(
			RvCategoryType<BlockInteractingRecipe> type,
			ResourceLocation id,
			RVCategoryHandler rvHandler
	) {
		super(type, id, rvHandler, INPUT_BLOCK_POSITION, VectorExtensions.withX(METHOD_POSITION, 27), INGREDIENT_POSITION);
	}

	@Override
	protected <R extends BlockKeyableRecipe> BlockState getRenderingBlock(R recipe) {
		return Blocks.FIRE.defaultBlockState();
	}

	@Override
	protected boolean shouldRenderInputBlockTooltip(BlockInteractingRecipe recipe) {
		return false;
	}
}
