package snownee.lychee.compat.jei.category;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.RecipeTypes;
import snownee.lychee.recipes.ItemInsideRecipe;
import snownee.lychee.util.ClientProxy;

@NotNullByDefault
public class ItemInsideRecipeCategory extends ItemAndBlockBaseCategory<ItemInsideRecipe> {

	public ItemInsideRecipeCategory(RecipeType<RecipeHolder<ItemInsideRecipe>> recipeType, IDrawable icon) {
		super(recipeType, icon, RecipeTypes.ITEM_INSIDE);
		infoRect.setPosition(0, 25);
		inputBlockRect.setX(80);
		methodRect.setX(77);
	}

	@Override
	public void drawExtra(RecipeHolder<ItemInsideRecipe> recipeHolder, GuiGraphics graphics, double mouseX, double mouseY, int centerX) {
		super.drawExtra(recipeHolder, graphics, mouseX, mouseY, centerX);
		ItemInsideRecipe recipe = recipeHolder.value();
		if (recipe.time() > 0) {
			var component = ClientProxy.format("tip.lychee.sec", recipe.time());
			var font = Minecraft.getInstance().font;
			ClientProxy.drawCenteredStringNoShadow(graphics, font, component, methodRect.getX() + 10, methodRect.getY() - 8, 0x666666);
		}
	}

	@Override
	public int contentWidth() {
		return WIDTH + 50;
	}

	@Override
	protected void renderIngredientGroup(IRecipeLayoutBuilder builder, ItemInsideRecipe recipe, int y) {
		ingredientGroup(builder, recipe, 40, y);
	}
}
