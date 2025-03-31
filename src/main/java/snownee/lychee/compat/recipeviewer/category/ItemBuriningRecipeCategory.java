package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2fc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.compat.recipeviewer.RVHelper;
import snownee.lychee.recipes.ItemBurningRecipe;
import snownee.lychee.util.VectorExtensions;

public class ItemBuriningRecipeCategory extends ItemAndBlockCategory<ItemBurningRecipe> {
	private static final Vector2fc METHOD_POSITION = VectorExtensions.offsetX(ItemAndBlockCategory.METHOD_POSITION, 15);

	public ItemBuriningRecipeCategory(
			RvCategoryType<ItemBurningRecipe> type,
			ResourceLocation id,
			RVHelper rvHandler
	) {
		super(type, id, rvHandler);
	}

	@Override
	protected BlockState getRenderingBlock(ItemBurningRecipe recipe) {
		return Blocks.FIRE.defaultBlockState();
	}

	@Override
	protected boolean shouldRenderInputBlockTooltip(ItemBurningRecipe recipe) {
		return false;
	}

	@Override
	public Vector2fc methodPosition() {
		return METHOD_POSITION;
	}
}
