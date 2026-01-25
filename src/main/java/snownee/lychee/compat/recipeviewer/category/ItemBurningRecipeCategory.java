package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2fc;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.RecipeTypes;
import snownee.lychee.recipes.ItemBurningRecipe;
import snownee.lychee.util.VectorExtensions;


public class ItemBurningRecipeCategory extends ItemAndBlockCategory<ItemBurningRecipe> {
	private static final Vector2fc INPUT_BLOCK_POSITION = VectorExtensions.offsetX(ItemAndBlockCategory.INPUT_BLOCK_POSITION, 12);
	private static final Vector2fc METHOD_POSITION = VectorExtensions.offsetX(ItemAndBlockCategory.METHOD_POSITION, 12);
	private static final Vector2fc INFO_POSITION = VectorExtensions.offset(METHOD_POSITION, METHOD_SIZE, 4);

	public ItemBurningRecipeCategory() {
		super(RecipeTypes.ITEM_BURNING);
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
	public Vector2fc inputBlockPosition() {
		return INPUT_BLOCK_POSITION;
	}

	@Override
	public Vector2fc methodPosition() {
		return METHOD_POSITION;
	}

	@Override
	public Vector2fc infoPosition(ItemBurningRecipe recipe) {
		return INFO_POSITION;
	}
}
