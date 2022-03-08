package snownee.lychee.compat.jei.category;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.Rect2i;
import snownee.lychee.block_exploding.BlockExplodingContext;
import snownee.lychee.block_exploding.BlockExplodingRecipe;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.core.recipe.type.LycheeRecipeType;

public class BlockExplodingRecipeCategory extends ItemAndBlockBaseCategory<BlockExplodingContext, BlockExplodingRecipe> {

	public BlockExplodingRecipeCategory(LycheeRecipeType<BlockExplodingContext, BlockExplodingRecipe> recipeType, ScreenElement mainIcon) {
		super(List.of(recipeType), mainIcon);
		infoRect = new Rect2i(0, 25, 8, 8);
	}

	@Override
	protected int getBlockRenderPosX() {
		return 18;
	}

	@Override
	protected int getBlockRenderPosY() {
		return 30;
	}

	@Override
	public void drawExtra(BlockExplodingRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {

	}

}
