package snownee.lychee.util;

import com.google.common.collect.ImmutableList;

import snownee.lychee.recipes.EntityTickingRecipe;

public interface LycheeEntityType {
	ImmutableList<EntityTickingRecipe> lychee$tickingRecipes();

	void lychee$setTickingRecipes(ImmutableList<EntityTickingRecipe> recipes);
}
