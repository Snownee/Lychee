package snownee.lychee.compat.rei.display;

import java.util.Map;

import com.google.common.collect.Maps;

import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.compat.rv.category.RvCategory;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public interface DisplayRegisters {
	Map<ResourceLocation, DisplayRegister<?>> ALL = Maps.newHashMap();

	DisplayRegister<ILycheeRecipe<LycheeContext>> DEFAULT = (registry, id, category) -> {
		for (var recipe : category.recipes()) {
			registry.add(new SimpleLycheeDisplay<>(recipe, id));
		}
	};

	static <R extends ILycheeRecipe<LycheeContext>> DisplayRegister<R> get(ResourceLocation id) {
		//noinspection unchecked
		return (DisplayRegister<R>) ALL.getOrDefault(id, DEFAULT);
	}

	static <R extends ILycheeRecipe<LycheeContext>> DisplayRegister<R> register(
			LycheeRecipeType<R> type,
			DisplayRegister<R> provider) {
		ALL.put(type.categoryId, provider);
		return provider;
	}

	@FunctionalInterface
	interface DisplayRegister<R extends ILycheeRecipe<LycheeContext>> {
		void consume(
				DisplayRegistry registry,
				CategoryIdentifier<? extends LycheeDisplay<R>> id,
				RvCategory<R> category);
	}
}
