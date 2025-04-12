package snownee.lychee.compat.recipeviewer.jei.category;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.platform.InputConstants;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiKeyMapping;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.input.IInternalKeyMappings;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import snownee.lychee.compat.recipeviewer.RvHelper;
import snownee.lychee.util.ui.InputAction;


public class JeiRvHelper extends RvHelper {
	public static final JeiRvHelper INSTANCE = new JeiRvHelper();
	private @Nullable IJeiRuntime runtime;
	private @Nullable IJeiHelpers jeiHelpers;

	@Override
	public boolean doAction(ItemStack stack, InputAction.Direct action) {
		if (runtime == null) {
			return false;
		}
		var recipesGui = runtime.getRecipesGui();
		var focusFactory = runtime.getJeiHelpers().getFocusFactory();
		var role = action == InputAction.Direct.SHOW_USAGES ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
		if (!stack.isEmpty()) {
			recipesGui.show(focusFactory.createFocus(role, VanillaTypes.ITEM_STACK, stack));
			return true;
		}
		return false;
	}

	@Override
	public boolean doAction(Fluid fluid, InputAction.Direct action) {
		if (runtime == null) {
			return false;
		}
		var recipesGui = runtime.getRecipesGui();
		var helpers = runtime.getJeiHelpers();
		//noinspection unchecked
		var fluidHelper = (IPlatformFluidHelper<FluidStack>) helpers.getPlatformFluidHelper();
		var focusFactory = helpers.getFocusFactory();
		var role = action == InputAction.Direct.SHOW_USAGES ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
		//noinspection deprecation
		recipesGui.show(focusFactory.createFocus(
				role,
				fluidHelper.getFluidIngredientType(),
				fluidHelper.create(fluid.builtInRegistryHolder(), fluidHelper.bucketVolume())
		));
		return true;
	}

	@Override
	public Optional<InputAction.Direct> toDirectAction(InputAction action) {
		if (action instanceof InputAction.Direct direct) {
			return Optional.of(direct);
		}
		if (runtime == null) {
			return Optional.empty();
		}
		try {
			IInternalKeyMappings keyMappings = (IInternalKeyMappings) runtime.getKeyMappings();
			List<Map.Entry<IJeiKeyMapping, InputAction.Direct>> keys = List.of(
					Map.entry(keyMappings.getShowRecipe(), InputAction.Direct.SHOW_RECIPES),
					Map.entry(keyMappings.getShowUses(), InputAction.Direct.SHOW_USAGES),
					Map.entry(keyMappings.getBookmark(), InputAction.Direct.FAVORITE));
			for (Map.Entry<IJeiKeyMapping, InputAction.Direct> entry : keys) {
				InputConstants.Key key = action.keyMapping();
				if (key != InputConstants.UNKNOWN && entry.getKey().isActiveAndMatches(key)) {
					return Optional.ofNullable(entry.getValue());
				}
			}
		} catch (Exception ignored) {
		}
		return Optional.empty();
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
