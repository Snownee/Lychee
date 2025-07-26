package snownee.lychee.compat.recipeviewer.emi.category;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.blaze3d.platform.InputConstants;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.config.EmiConfig;
import dev.emi.emi.input.EmiBind;
import dev.emi.emi.runtime.EmiFavorites;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import snownee.jade.api.config.IWailaConfig;
import snownee.lychee.compat.recipeviewer.RvHelper;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.ui.InputAction;


public class EmiRvHelper extends RvHelper {
	public static final EmiRvHelper INSTANCE = new EmiRvHelper();

	@Override
	public boolean doAction(ItemStack stack, InputAction.Direct action) {
		return doAction(EmiStack.of(stack), action);
	}

	@Override
	public boolean doAction(Fluid fluid, InputAction.Direct action) {
		return doAction(EmiStack.of(fluid), action);
	}

	public boolean doAction(EmiIngredient ingredient, InputAction.Direct action) {
		if (action == InputAction.Direct.SHOW_USAGES) {
			EmiApi.displayUses(ingredient);
		} else if (action == InputAction.Direct.SHOW_RECIPES) {
			EmiApi.displayRecipes(ingredient);
		} else if (action == InputAction.Direct.FAVORITE) {
			EmiFavorites.addFavorite(ingredient);
		}
		return false;
	}

	@Override
	public Optional<InputAction.Direct> toDirectAction(InputAction action) {
		if (action instanceof InputAction.Direct direct) {
			return Optional.of(direct);
		}
		try {
			List<Map.Entry<EmiBind, InputAction.Direct>> keys = List.of(
					Map.entry(EmiConfig.viewRecipes, InputAction.Direct.SHOW_RECIPES),
					Map.entry(EmiConfig.viewUses, InputAction.Direct.SHOW_USAGES),
					Map.entry(EmiConfig.favorite, InputAction.Direct.FAVORITE));
			for (Map.Entry<EmiBind, InputAction.Direct> entry : keys) {
				InputConstants.Key key = action.keyMapping();
				if (key != InputConstants.UNKNOWN && entry.getKey().isHeld()) {
					return Optional.ofNullable(entry.getValue());
				}
			}
		} catch (Exception ignored) {
		}
		return Optional.empty();
	}

	@Override
	public boolean appendModName() {
		if (ClientProxy.hasJade && IWailaConfig.get().getGeneral().showItemModNameTooltip()) {
			return true;
		}
		return EmiConfig.appendModId;
	}
}
