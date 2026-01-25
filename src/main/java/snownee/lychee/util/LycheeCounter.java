package snownee.lychee.util;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public interface LycheeCounter {

	void lychee$setRecipeId(@Nullable ResourceKey<Recipe<?>> id);

	@Nullable ResourceKey<Recipe<?>> lychee$getRecipeId();

	void lychee$setCount(int count);

	int lychee$getCount();

	default <T extends Recipe<?>> void lychee$update(@Nullable ResourceKey<Recipe<?>> prevId, RecipeHolder<T> recipe) {
		lychee$setRecipeId(recipe.id());
		if (Objects.equals(prevId, recipe.id())) {
			lychee$setCount(lychee$getCount() + 1);
		} else {
			lychee$setCount(0);
		}
	}
}
