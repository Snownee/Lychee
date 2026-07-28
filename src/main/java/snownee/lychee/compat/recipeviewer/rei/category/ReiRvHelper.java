package snownee.lychee.compat.recipeviewer.rei.category;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.blaze3d.platform.InputConstants;

import me.shedaniel.clothconfig2.api.ModifierKeyCode;
import me.shedaniel.rei.api.client.config.ConfigObject;
import me.shedaniel.rei.api.client.favorites.FavoriteEntry;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import snownee.lychee.compat.recipeviewer.RvHelper;
import snownee.lychee.util.ui.InputAction;

public class ReiRvHelper extends RvHelper {
	public static final ReiRvHelper INSTANCE = new ReiRvHelper();

	@Override
	public boolean doAction(ItemStack stack, InputAction.Direct action) {
		if (stack.isEmpty()) {
			return false;
		}
		return doAction(EntryStacks.of(stack), action);
	}

	@Override
	public boolean doAction(Fluid fluid, InputAction.Direct action) {
		return doAction(EntryStacks.of(fluid), action);
	}

	@Override
	public Optional<InputAction.Direct> toDirectAction(InputAction action) {
		if (action instanceof InputAction.Direct direct) {
			return Optional.of(direct);
		}
		ConfigObject configObject = ConfigObject.getInstance();
		List<Map.Entry<ModifierKeyCode, InputAction.Direct>> keys = List.of(
				Map.entry(configObject.getRecipeKeybind(), InputAction.Direct.SHOW_RECIPES),
				Map.entry(configObject.getUsageKeybind(), InputAction.Direct.SHOW_USAGES),
				Map.entry(configObject.getFavoriteKeyCode(), InputAction.Direct.FAVORITE));
		InputConstants.Key key = action.keyMapping();
		for (Map.Entry<ModifierKeyCode, InputAction.Direct> entry : keys) {
			if (key.getType() == InputConstants.Type.MOUSE) {
				if (entry.getKey().matchesMouse(key.getValue())) {
					return Optional.of(entry.getValue());
				}
			} else if (key != InputConstants.UNKNOWN && entry.getKey().matchesKey(key.getValue(), key.getValue())) {
				return Optional.of(entry.getValue());
			}
		}
		if (key.getType() == InputConstants.Type.MOUSE) {
			int button = key.getValue();
			if (configObject.getRecipeKeybind().getType() != InputConstants.Type.MOUSE && button == 0) {
				return Optional.of(InputAction.Direct.SHOW_RECIPES);
			} else if (configObject.getUsageKeybind().getType() != InputConstants.Type.MOUSE && button == 1) {
				return Optional.of(InputAction.Direct.SHOW_USAGES);
			}
		}
		return Optional.empty();
	}

	@Override
	public boolean appendModName() {
		return ConfigObject.getInstance().shouldAppendModNames();
	}

	@SuppressWarnings("UnstableApiUsage")
	public boolean doAction(EntryStack<?> entry, InputAction.Direct action) {
		if (action == InputAction.Direct.SHOW_RECIPES) {
			return ViewSearchBuilder.builder().addRecipesFor(entry).open();
		} else if (action == InputAction.Direct.SHOW_USAGES) {
			return ViewSearchBuilder.builder().addUsagesFor(entry).open();
		} else if (action == InputAction.Direct.FAVORITE) {
			FavoriteEntry favoriteEntry = FavoriteEntry.fromEntryStack(entry);
			ConfigObject.getInstance().getFavoriteEntries().add(favoriteEntry);
			return true;
		}
		return false;
	}
}
