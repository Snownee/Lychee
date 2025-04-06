package snownee.lychee.compat.recipeviewer.category;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2fc;

import net.minecraft.resources.ResourceLocation;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RvHelper;
import snownee.lychee.compat.recipeviewer.element.InfoElementHelper;
import snownee.lychee.recipes.BlockExplodingRecipe;
import snownee.lychee.util.VectorExtensions;


public class BlockExplodingRecipeCategory extends ItemAndBlockCategory<BlockExplodingRecipe> {
	public static final Vector2fc INPUT_BLOCK_POSITION = new Vector2f(22, 20);
	public static final Vector2fc INFO_POSITION = VectorExtensions.offset(INPUT_BLOCK_POSITION, -InfoElementHelper.INFO_SIZE - 4, 0);

	protected BlockExplodingRecipeCategory(
			RvCategoryType<BlockExplodingRecipe> type,
			ResourceLocation id,
			RvHelper rvHandler
	) {
		super(type, id, rvHandler);
	}

	@Nullable
	@Override
	protected RenderElement getMethodElement(BlockExplodingRecipe recipe) {
		return null;
	}

	@Override
	public Vector2fc inputBlockPosition() {
		return INPUT_BLOCK_POSITION;
	}

	@Override
	public Vector2fc infoPosition() {
		return INFO_POSITION;
	}
}
