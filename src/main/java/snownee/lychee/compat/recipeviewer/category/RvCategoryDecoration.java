package snownee.lychee.compat.recipeviewer.category;

import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.recipes.BlockExplodingRecipe;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public interface RvCategoryDecoration<R extends ILycheeRecipe<LycheeContext>> {
	RvCategoryDecoration<BlockExplodingRecipe> NOTHING = (builder, recipeHolder) -> {
	};

	void setup(RvCategoryWidgetBuilder builder, RecipeHolder<R> recipeHolder);
}
