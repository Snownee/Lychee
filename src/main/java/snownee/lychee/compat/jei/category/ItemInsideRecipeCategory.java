package snownee.lychee.compat.jei.category;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.item_inside.ItemInsideRecipe;
import snownee.lychee.item_inside.ItemInsideRecipeType;
import snownee.lychee.util.LUtil;

public class ItemInsideRecipeCategory extends ItemAndBlockBaseCategory<ItemShapelessContext, ItemInsideRecipe> {

	public ItemInsideRecipeCategory(ItemInsideRecipeType recipeType, ScreenElement mainIcon) {
		super(List.of(recipeType), mainIcon);
		infoRect.setPosition(0, 25);
		inputBlockRect.setX(80);
		methodRect.setX(77);
	}

	@Override
	public void drawExtra(ItemInsideRecipe recipe, PoseStack matrixStack, double mouseX, double mouseY, int centerX) {
		super.drawExtra(recipe, matrixStack, mouseX, mouseY, centerX);
		if (recipe.getTime() > 0) {
			Component component = LUtil.format("tip.lychee.sec", recipe.getTime());
			Font font = Minecraft.getInstance().font;
			font.draw(matrixStack, component, methodRect.getX() + 10 - font.width(component) / 2, methodRect.getY() - 8, 0x666666);
		}
	}

	@Override
	public int getWidth() {
		return width + 50;
	}

}
