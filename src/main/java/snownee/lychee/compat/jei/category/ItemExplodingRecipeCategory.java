package snownee.lychee.compat.jei.category;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.world.item.Items;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.item_exploding.ItemExplodingRecipe;

public class ItemExplodingRecipeCategory extends ItemShapelessRecipeCategory<ItemExplodingRecipe> {

	public ItemExplodingRecipeCategory(LycheeRecipeType<ItemShapelessContext, ItemExplodingRecipe> recipeType) {
		super(recipeType);
	}

	@Override
	public IDrawable createIcon(IGuiHelper guiHelper) {
		return guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.TNT.getDefaultInstance());
	}

}
