package snownee.lychee.util.contextual;

import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.util.recipe.OldLycheeRecipe;

public interface RecipeCondition {
	/**
	 * @param recipe Current recipe
	 * @param ctx    Context
	 * @param times  Time of request to execute
	 *
	 * @return Executable time after condition
	 */
	int test(RecipeHolder<OldLycheeRecipe<?>> recipe, LycheeRecipeContext ctx, int times);
}