package snownee.lychee.compat.recipeviewer.category;

import java.util.Set;

import org.joml.Vector2fc;

import snownee.lychee.util.recipe.ILycheeRecipe;

public abstract class RvCategoryLayoutBuilder extends RvCategoryBuilder {
	protected RvCategoryLayoutBuilder(RvCategoryInstance<?> instance) {
		super(instance);
	}

	public abstract void actionGroup(ILycheeRecipe<?> recipe, Vector2fc position);

	public abstract void ingredientGroup(ILycheeRecipe<?> recipe, Vector2fc position);

	public static abstract class Wrapped extends RvCategoryLayoutBuilder {
		private final boolean actionGroup;
		private final boolean ingredientGroup;

		protected Wrapped(RvCategoryInstance<?> instance) {
			super(instance);
			Set<String> decorationKeys = instance.decorations().keySet();
			actionGroup = !decorationKeys.contains("action_group");
			ingredientGroup = !decorationKeys.contains("ingredient_group");
		}

		protected abstract void _actionGroup(ILycheeRecipe<?> recipe, Vector2fc position);

		protected abstract void _ingredientGroup(ILycheeRecipe<?> recipe, Vector2fc position);

		@Override
		public void actionGroup(ILycheeRecipe<?> recipe, Vector2fc position) {
			if (actionGroup) {
				_actionGroup(recipe, position);
			}
		}

		@Override
		public void ingredientGroup(ILycheeRecipe<?> recipe, Vector2fc position) {
			if (ingredientGroup) {
				_ingredientGroup(recipe, position);
			}
		}
	}
}
