package snownee.lychee.util;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.recipes.EntityTickingRecipe;

public interface LycheeEntityType {
	ImmutableList<RecipeHolder<EntityTickingRecipe>> lychee$tickingRecipes();

	void lychee$setTickingRecipes(ImmutableList<RecipeHolder<EntityTickingRecipe>> recipes);
}
