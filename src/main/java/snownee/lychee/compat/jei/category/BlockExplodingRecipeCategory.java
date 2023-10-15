package snownee.lychee.compat.jei.category;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import snownee.lychee.block_exploding.BlockExplodingContext;
import snownee.lychee.block_exploding.BlockExplodingRecipe;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.core.recipe.type.LycheeRecipeType;

public class BlockExplodingRecipeCategory extends ItemAndBlockBaseCategory<BlockExplodingContext, BlockExplodingRecipe> {

	public BlockExplodingRecipeCategory(LycheeRecipeType<BlockExplodingContext, BlockExplodingRecipe> recipeType, ScreenElement mainIcon) {
		super(List.of(recipeType), mainIcon);
		inputBlockRect = new Rect2i(15, 30, 20, 20);
	}

	@Override
	public void drawExtra(BlockExplodingRecipe recipe, GuiGraphics graphics, double mouseX, double mouseY, int centerX) {

	}

}
