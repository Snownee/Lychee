package snownee.lychee.util.action;

import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.util.recipe.OldLycheeRecipe;

public record Job(PostAction<?> action, int times) {
	public void apply(RecipeHolder<OldLycheeRecipe<?>> recipe, LycheeRecipeContext ctx) {
		int times = action.test(recipe, ctx, this.times);
		if (times > 0) {
			action.apply(recipe, ctx, times);
		} else {
			action.onFailure(recipe, ctx, this.times);
		}
	}
}
