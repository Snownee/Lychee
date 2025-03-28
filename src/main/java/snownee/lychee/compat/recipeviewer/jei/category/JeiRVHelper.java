package snownee.lychee.compat.recipeviewer.jei.category;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import snownee.lychee.compat.recipeviewer.RVHelper;
import snownee.lychee.compat.recipeviewer.jei.LycheeJEIPlugin;

public class JeiRVHelper extends RVHelper {
	public static final JeiRVHelper INSTANCE = new JeiRVHelper();

	@Override
	public boolean openPage(ItemStack stack, boolean usageOrRecipe) {
		var recipesGui = LycheeJEIPlugin.runtime.getRecipesGui();
		var focusFactory = LycheeJEIPlugin.helpers.getFocusFactory();
		var role = !usageOrRecipe ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
		if (!stack.isEmpty()) {
			recipesGui.show(focusFactory.createFocus(role, VanillaTypes.ITEM_STACK, stack));
			return true;
		}
		return false;
	}

	@Override
	public boolean openPage(Fluid fluid, boolean usageOrRecipe) {
		var fluidHelper = (IPlatformFluidHelper<IJeiFluidIngredient>) LycheeJEIPlugin.helpers.getPlatformFluidHelper();
		var recipesGui = LycheeJEIPlugin.runtime.getRecipesGui();
		var focusFactory = LycheeJEIPlugin.helpers.getFocusFactory();
		var role = !usageOrRecipe ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
		recipesGui.show(focusFactory.createFocus(
				role,
				fluidHelper.getFluidIngredientType(),
				fluidHelper.create(fluid.builtInRegistryHolder(), fluidHelper.bucketVolume())
		));
		return true;
	}
}
