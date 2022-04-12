package snownee.lychee.compat.rei.category;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.renderer.Rect2i;
import snownee.lychee.block_exploding.BlockExplodingContext;
import snownee.lychee.block_exploding.BlockExplodingRecipe;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.compat.rei.display.BlockExplodingDisplay;
import snownee.lychee.core.recipe.type.LycheeRecipeType;

public class BlockExplodingRecipeCategory extends ItemAndBlockBaseCategory<BlockExplodingContext, BlockExplodingRecipe, BlockExplodingDisplay> {

	public BlockExplodingRecipeCategory(LycheeRecipeType<BlockExplodingContext, BlockExplodingRecipe> recipeType, ScreenElement mainIcon) {
		super(List.of(recipeType), mainIcon);
		infoRect = new Rect2i(3, 25, 8, 8);
		inputBlockRect = new Rect2i(18, 30, 20, 20);
	}

	@Override
	public CategoryIdentifier<? extends BlockExplodingDisplay> getCategoryIdentifier() {
		return REICompat.BLOCK_EXPLODING;
	}

	@Override
	public void drawExtra(BlockExplodingRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY) {

	}

}
