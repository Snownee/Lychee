package snownee.lychee.util.contextual;

import org.jetbrains.annotations.Nullable;

import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.LycheeRecipe;

public interface RecipeCondition {
	/**
	 * @param recipe Current recipe
	 * @param ctx    Context
	 * @param times  Time of request to execute
	 *
	 * @return Executable time after condition
	 */
	int test(@Nullable LycheeRecipe<?> recipe, LycheeContext ctx, int times);
}
