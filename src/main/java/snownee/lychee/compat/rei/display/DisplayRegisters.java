package snownee.lychee.compat.rei.category;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.compat.rei.display.SimpleLycheeDisplay;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public interface DisplayRegisters {
	Map<ResourceLocation, DisplayRegister<?>> ALL = Maps.newHashMap();

	DisplayRegister<ILycheeRecipe<LycheeContext>> DEFAULT = (registry, category, recipes) -> {
		for (var recipe : recipes) {
			registry.add(new SimpleLycheeDisplay<>(recipe.value(), (CategoryIdentifier) category.categoryIdentifier()));
		}
	};

	static <R extends ILycheeRecipe<LycheeContext>> DisplayRegister<R> get(ResourceLocation id) {
		return (DisplayRegister<R>) ALL.getOrDefault(id, DEFAULT);
	}

	static <R extends ILycheeRecipe<LycheeContext>> DisplayRegister<R> register(
			LycheeRecipeType<LycheeContext, R> type,
			DisplayRegister<R> provider) {
		ALL.put(type.categoryId, provider);
		return provider;
	}

	@FunctionalInterface
	interface DisplayRegister<R extends ILycheeRecipe<LycheeContext>> {
		void consume(
				DisplayRegistry registry,
				LycheeDisplayCategory<? extends LycheeDisplay<R>> category,
				Collection<RecipeHolder<R>> recipes);
	}
}
