package snownee.lychee.compat.jei.category;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.RecipeTypes;
import snownee.lychee.block_exploding.BlockExplodingContext;
import snownee.lychee.block_exploding.BlockExplodingRecipe;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.core.def.BlockPredicateHelper;

public class BlockExplodingRecipeCategory extends ItemAndBlockBaseCategory<BlockExplodingContext, BlockExplodingRecipe> {

	public BlockExplodingRecipeCategory(IGuiHelper guiHelper, ScreenElement mainIcon) {
		super(List.of(RecipeTypes.BLOCK_EXPLODING), guiHelper, mainIcon);
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

	@Override
	public void setInputs(BlockExplodingRecipe recipe, IIngredients ingredients) {
		List<ItemStack> items1 = BlockPredicateHelper.getMatchedItemStacks(recipe.getBlock());
		if (!items1.isEmpty()) {
			ingredients.setInputLists(VanillaTypes.ITEM, List.of(items1));
		}
	}

}
