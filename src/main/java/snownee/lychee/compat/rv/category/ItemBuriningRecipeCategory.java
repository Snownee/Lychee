package snownee.lychee.compat.rv.category;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.recipes.ItemBurningRecipe;
import snownee.lychee.util.VectorExtensions;

public class ItemBuriningRecipeCategory extends ItemAndBlockCategory<ItemBurningRecipe> {
	public ItemBuriningRecipeCategory(
			RvCategoryType<ItemBurningRecipe> type,
			ResourceLocation id,
			RVCategoryHandler rvHandler
	) {
		super(type, id, rvHandler, INPUT_BLOCK_POSITION, VectorExtensions.withX(METHOD_POSITION, 27), INGREDIENT_POSITION);
	}

	@Override
	protected BlockState getRenderingBlock(ItemBurningRecipe recipe) {
		return Blocks.FIRE.defaultBlockState();
	}

	@Override
	protected boolean shouldRenderInputBlockTooltip(ItemBurningRecipe recipe) {
		return false;
	}
}
