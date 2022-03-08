package snownee.lychee.compat.jei.category;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ItemAndBlockRecipe;
import snownee.lychee.core.recipe.type.BlockKeyRecipeType;
import snownee.lychee.item_inside.ItemInsideRecipe;
import snownee.lychee.util.LUtil;

public class ItemInsideRecipeCategory<T extends ItemAndBlockRecipe<LycheeContext>> extends ItemAndBlockBaseCategory<LycheeContext, T> {

	public ItemInsideRecipeCategory(BlockKeyRecipeType<LycheeContext, T> recipeType, ScreenElement mainIcon) {
		this(List.of(recipeType), mainIcon);
	}

	@SuppressWarnings("rawtypes")
	public ItemInsideRecipeCategory(List<BlockKeyRecipeType<LycheeContext, T>> recipeTypes, ScreenElement mainIcon) {
		super((List) recipeTypes, mainIcon);
	}

	@Override
	public void drawExtra(T recipe, PoseStack matrixStack, double mouseX, double mouseY) {
		super.drawExtra(recipe, matrixStack, mouseX, mouseY);
		ItemInsideRecipe insideRecipe = (ItemInsideRecipe) recipe;
		if (insideRecipe.getTime() > 0) {
			Minecraft.getInstance().font.draw(matrixStack, LUtil.format("tip.lychee.sec", insideRecipe.getTime()), 0, 0, 0);
		}
	}

}
