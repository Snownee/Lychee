package snownee.lychee.compat.jei.category;

import java.util.List;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Items;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.item_exploding.ItemExplodingRecipe;

public class ItemExplodingRecipeCategory extends ItemShapelessRecipeCategory<ItemExplodingRecipe> {

	public ItemExplodingRecipeCategory(LycheeRecipeType<ItemShapelessContext, ItemExplodingRecipe> recipeType) {
		super(recipeType);
	}

	@Override
	public IDrawable createIcon(IGuiHelper guiHelper, List<ItemExplodingRecipe> recipes) {
		return guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.TNT.getDefaultInstance());
	}

	@Override
	public void draw(ItemExplodingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
		drawInfoBadge(recipe, graphics, mouseX, mouseY);
		JEIREI.renderTnt(graphics, 85, 34);
	}

}
