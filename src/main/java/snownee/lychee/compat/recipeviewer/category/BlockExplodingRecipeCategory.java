package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import snownee.lychee.compat.recipeviewer.element.InfoElementHelper;
import snownee.lychee.recipes.BlockExplodingRecipe;
import snownee.lychee.util.VectorExtensions;


public class BlockExplodingRecipeCategory extends ItemAndBlockCategory<BlockExplodingRecipe> {
	public static final Vector2fc INPUT_BLOCK_POSITION = new Vector2f(22, 20);
	public static final Vector2fc INFO_POSITION = VectorExtensions.offset(INPUT_BLOCK_POSITION, -InfoElementHelper.INFO_SIZE - 4, 0);

	@Override
	public void setupDecorations(DecorationMapBuilder<BlockExplodingRecipe> mapBuilder) {
		super.setupDecorations(mapBuilder);
		mapBuilder.put("method", RvCategoryDecoration.NOTHING);
	}

	@Override
	public Vector2fc inputBlockPosition() {
		return INPUT_BLOCK_POSITION;
	}

	@Override
	public Vector2fc infoPosition(BlockExplodingRecipe recipe) {
		return INFO_POSITION;
	}
}
