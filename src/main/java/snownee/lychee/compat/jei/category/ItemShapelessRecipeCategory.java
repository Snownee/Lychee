package snownee.lychee.compat.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.client.renderer.Rect2i;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.core.recipe.ItemShapelessRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;

public abstract class ItemShapelessRecipeCategory<T extends ItemShapelessRecipe> extends BaseJEICategory<ItemShapelessContext, T> {

	public ItemShapelessRecipeCategory(LycheeRecipeType<ItemShapelessContext, T> recipeType) {
		super(recipeType);
		infoRect = new Rect2i(0, 25, 8, 8);
	}

	@Override
	public int getWidth() {
		return width + 50;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
		int xCenter = getWidth() / 2;
		int y = recipe.getIngredients().size() > 9 || recipe.getShowingPostActions().size() > 9 ? 26 : 28;
		ingredientGroup(builder, recipe, xCenter - 45, y);
		actionGroup(builder, recipe, xCenter + 50, y);
	}

	@Override
	public void draw(T recipe, IRecipeSlotsView recipeSlotsView, PoseStack matrixStack, double mouseX, double mouseY) {
		super.draw(recipe, recipeSlotsView, matrixStack, mouseX, mouseY);
		matrixStack.pushPose();
		matrixStack.translate(76, 16, 0);
		matrixStack.scale(1.5F, 1.5F, 0);
		icon.draw(matrixStack);
		matrixStack.popPose();
	}

}