package snownee.lychee.compat.jei.category;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.world.item.Items;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.lightning_channeling.LightningChannelingRecipe;

public class LightningChannelingRecipeCategory extends ItemShapelessRecipeCategory<LightningChannelingRecipe> {

	public LightningChannelingRecipeCategory(LycheeRecipeType<ItemShapelessContext, LightningChannelingRecipe> recipeType) {
		super(recipeType);
	}

	@Override
	public IDrawable createIcon(IGuiHelper guiHelper) {
		return guiHelper.createDrawableIngredient(VanillaTypes.ITEM, Items.LIGHTNING_ROD.getDefaultInstance());
	}

}
