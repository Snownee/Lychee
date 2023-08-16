package snownee.lychee.compat.rei.category;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.Rect2i;
import snownee.lychee.block_exploding.BlockExplodingContext;
import snownee.lychee.block_exploding.BlockExplodingRecipe;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.rei.display.BaseREIDisplay;
import snownee.lychee.core.recipe.type.LycheeRecipeType;

public class BlockExplodingRecipeCategory extends ItemAndBlockBaseCategory<BlockExplodingContext, BlockExplodingRecipe, BaseREIDisplay<BlockExplodingRecipe>> {

	public BlockExplodingRecipeCategory(LycheeRecipeType<BlockExplodingContext, BlockExplodingRecipe> recipeType, ScreenElement mainIcon) {
		super(List.of(recipeType), mainIcon);
		inputBlockRect = new Rect2i(18, 30, 20, 20);
	}

	@Override
	public void drawExtra(BlockExplodingRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY, int centerX) {

	}

}
