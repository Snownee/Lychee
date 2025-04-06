package snownee.lychee.compat.recipeviewer.jei.category;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.lychee.compat.recipeviewer.RvHelper;


public class JeiRvHelper extends RvHelper {
	public static final JeiRvHelper INSTANCE = new JeiRvHelper();
	private @Nullable IJeiRuntime runtime;
	private @Nullable IJeiHelpers jeiHelpers;

	@Override
	public boolean openPage(ItemStack stack, boolean usageOrRecipe) {
		if (runtime == null) {
			return false;
		}
		var recipesGui = runtime.getRecipesGui();
		var focusFactory = runtime.getJeiHelpers().getFocusFactory();
		var role = !usageOrRecipe ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
		if (!stack.isEmpty()) {
			recipesGui.show(focusFactory.createFocus(role, VanillaTypes.ITEM_STACK, stack));
			return true;
		}
		return false;
	}

	@Override
	public boolean openPage(Fluid fluid, boolean usageOrRecipe) {
		if (runtime == null) {
			return false;
		}
		var recipesGui = runtime.getRecipesGui();
		var helpers = runtime.getJeiHelpers();
		//noinspection unchecked
		var fluidHelper = (IPlatformFluidHelper<FluidStack>) helpers.getPlatformFluidHelper();
		var focusFactory = helpers.getFocusFactory();
		var role = !usageOrRecipe ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
		recipesGui.show(focusFactory.createFocus(
				role,
				fluidHelper.getFluidIngredientType(),
				fluidHelper.create(fluid.builtInRegistryHolder(), fluidHelper.bucketVolume())
		));
		return true;
	}

	@ApiStatus.Internal
	public void setRuntime(@Nullable IJeiRuntime runtime) {
		this.runtime = runtime;
	}

	@ApiStatus.Internal
	public void setJeiHelpers(@Nullable IJeiHelpers jeiHelpers) {
		this.jeiHelpers = jeiHelpers;
	}

	public IJeiRuntime jeiRuntime() {
		return Objects.requireNonNull(runtime);
	}

	public IJeiHelpers jeiHelpers() {
		return Objects.requireNonNull(jeiHelpers);
	}
}
