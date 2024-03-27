package snownee.lychee.compat.rei.display;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.compat.rei.category.LycheeDisplayCategory;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public interface DisplayRegisters {
	Map<ResourceLocation, DisplayRegister<?>> ALL = Maps.newHashMap();

	DisplayRegister<ILycheeRecipe<?>> DEFAULT = (registry, category, recipes) -> {
		for (var recipe : recipes) {
			registry.add(new SimpleLycheeDisplay<>(recipe.value(), (CategoryIdentifier) category.getCategoryIdentifier()));
		}
	};


	static <R extends ILycheeRecipe<?>> DisplayRegister<R> get(ResourceLocation id) {
		return (DisplayRegister<R>) ALL.getOrDefault(id, DEFAULT);
	}

	static <R extends ILycheeRecipe<?>> DisplayRegister<R> register(
			LycheeRecipeType<?, R> type,
			DisplayRegister<R> provider) {
		ALL.put(type.categoryId, provider);
		return provider;
	}

	@FunctionalInterface
	interface DisplayRegister<R extends ILycheeRecipe<?>> {
		void consume(
				DisplayRegistry registry,
				LycheeDisplayCategory<? extends LycheeDisplay<R>> category,
				Collection<RecipeHolder<R>> recipes);
	}
}
