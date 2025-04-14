package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2fc;

import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public abstract class RvCategoryLayoutBuilder<R extends ILycheeRecipe<LycheeContext>> extends RvCategoryBuilder<R> {
	protected RvCategoryLayoutBuilder(RvCategoryInstance<R> instance, RecipeHolder<R> recipeHolder) {
		super(instance, recipeHolder);
	}

	public abstract void actionGroup(R recipe, Vector2fc position);

	public abstract void ingredientGroup(R recipe, Vector2fc position);

	public static abstract class Wrapped<R extends ILycheeRecipe<LycheeContext>> extends RvCategoryLayoutBuilder<R> {
		private final boolean actionGroup;
		private final boolean ingredientGroup;

		protected Wrapped(RvCategoryInstance<R> instance, RecipeHolder<R> recipeHolder) {
			super(instance, recipeHolder);
			actionGroup = !decorations().containsKey("action_group");
			ingredientGroup = !decorations().containsKey("ingredient_group");
		}

		protected abstract void _actionGroup(R recipe, Vector2fc position);

		protected abstract void _ingredientGroup(R recipe, Vector2fc position);

		@Override
		public void actionGroup(R recipe, Vector2fc position) {
			if (actionGroup) {
				_actionGroup(recipe, position);
			}
		}

		@Override
		public void ingredientGroup(R recipe, Vector2fc position) {
			if (ingredientGroup) {
				_ingredientGroup(recipe, position);
			}
		}
	}
}
