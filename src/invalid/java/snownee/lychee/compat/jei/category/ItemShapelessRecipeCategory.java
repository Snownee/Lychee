package snownee.lychee.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.client.gui.GuiGraphics;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.core.recipe.ItemShapelessRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;

public abstract class ItemShapelessRecipeCategory<T extends ItemShapelessRecipe<T>> extends BaseJEICategory<ItemShapelessContext, T> {

	public ItemShapelessRecipeCategory(LycheeRecipeType<ItemShapelessContext, T> recipeType) {
		super(recipeType);
	}

	@Override
	public int getWidth() {
		return width + 50;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
		int xCenter = getWidth() / 2;
		int y = recipe.getIngredients().size() > 9 || recipe.conditions().showingCount() > 9 ? 26 : 28;
		ingredientGroup(builder, recipe, xCenter - 45, y);
		actionGroup(builder, recipe, xCenter + 50, y);
		addBlockIngredients(builder, recipe);
	}

	@Override
	public void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		drawInfoBadgeIfNeeded(recipe, graphics, mouseX, mouseY);
		PoseStack matrixStack = graphics.pose();
		matrixStack.pushPose();
		matrixStack.translate(76, 16, 0);
		matrixStack.scale(1.5F, 1.5F, 1.5F);
		getIcon().draw(graphics);
		matrixStack.popPose();
	}

}
