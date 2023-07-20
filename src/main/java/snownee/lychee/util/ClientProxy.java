package snownee.lychee.util;

import java.text.MessageFormat;
import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import snownee.lychee.core.recipe.ILycheeRecipe;

public interface ClientProxy {
	List<RecipeViewerWidgetClickListener> recipeViewerWidgetClickListeners = ObjectArrayList.of();

	static MutableComponent getDimensionDisplayName(ResourceKey<Level> dimension) {
		String key = Util.makeDescriptionId("dimension", dimension.location());
		if (I18n.exists(key)) {
			return Component.translatable(key);
		} else {
			return Component.literal(LUtil.capitaliseAllWords(dimension.location().getPath()));
		}
	}

	static MutableComponent getStructureDisplayName(ResourceLocation rawName) {
		String key = Util.makeDescriptionId("structure", rawName);
		if (I18n.exists(key)) {
			return Component.translatable(key);
		} else {
			return Component.literal(LUtil.capitaliseAllWords(rawName.getPath()));
		}
	}

	static MutableComponent format(String s, Object... objects) {
		try {
			return Component.literal(MessageFormat.format(I18n.get(s), objects));
		} catch (Exception e) {
			return Component.translatable(s, objects);
		}
	}

	static void registerInfoBadgeClickListener(RecipeViewerWidgetClickListener listener) {
		synchronized (recipeViewerWidgetClickListeners) {
			recipeViewerWidgetClickListeners.add(listener);
		}
	}

	static boolean postInfoBadgeClickEvent(ILycheeRecipe<?> recipe, int button) {
		for (RecipeViewerWidgetClickListener listener : recipeViewerWidgetClickListeners) {
			if (listener.onClick(recipe, button)) {
				return true;
			}
		}
		return false;
	}

	@FunctionalInterface
	interface RecipeViewerWidgetClickListener {
		boolean onClick(ILycheeRecipe<?> recipe, int button);
	}

}
