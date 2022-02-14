package snownee.lychee.core.recipe;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

public interface LycheeCounter {

	void lychee$setRecipeId(@Nullable ResourceLocation id);

	@Nullable
	ResourceLocation lychee$getRecipeId();

	void lychee$setCount(int count);

	int lychee$getCount();

	default void lychee$update(@Nullable ResourceLocation prevRecipeId, Recipe<?> recipe) {
		lychee$setRecipeId(recipe.getId());
		if (Objects.equals(prevRecipeId, recipe.getId())) {
			lychee$setCount(lychee$getCount() + 1);
		} else {
			lychee$setCount(0);
		}
	}

}