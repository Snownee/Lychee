package snownee.lychee.compat.recipeviewer.category;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public abstract class RvCategoryLayoutBuilder<R extends ILycheeRecipe<LycheeContext>> extends RvCategoryBuilder<R> {
	protected RvCategoryLayoutBuilder(RvCategoryInstance<R> instance, RecipeHolder<R> recipeHolder) {
		super(instance, recipeHolder);
	}

	public abstract void actionGroup(R recipe, Vector2fc position);

	public void ingredientGroup(R recipe, Vector2fc position) {
		ingredientGroup(recipe, (index, size) -> {
			var gridX = (int) Math.ceil(Math.sqrt(size));
			var gridY = (int) Math.ceil((float) size / gridX);
			return new Vector2f(
					position.x() - gridX * 9 + index % gridX * 19,
					position.y() - gridY * 9 + index / gridX * 19);
		});
	}

	/**
	 * Adds the input ingredient slots using category-local coordinates.
	 */
	public abstract void ingredientGroup(R recipe, IngredientLayout layout);

	@FunctionalInterface
	public interface IngredientLayout {
		Vector2fc position(int index, int size);
	}

	public static abstract class Wrapped<R extends ILycheeRecipe<LycheeContext>> extends RvCategoryLayoutBuilder<R> {
		private final boolean actionGroup;
		private final boolean ingredientGroup;

		protected Wrapped(RvCategoryInstance<R> instance, RecipeHolder<R> recipeHolder) {
			super(instance, recipeHolder);
			actionGroup = !decorations().containsKey("action_group");
			ingredientGroup = !decorations().containsKey("ingredient_group");
		}

		protected abstract void _actionGroup(R recipe, Vector2fc position);

		protected abstract void _ingredientGroup(R recipe, IngredientLayout layout);

		@Override
		public void actionGroup(R recipe, Vector2fc position) {
			if (actionGroup) {
				_actionGroup(recipe, position);
			}
		}

		@Override
		public void ingredientGroup(R recipe, Vector2fc position) {
			super.ingredientGroup(recipe, position);
		}

		@Override
		public void ingredientGroup(R recipe, IngredientLayout layout) {
			if (ingredientGroup) {
				_ingredientGroup(recipe, layout);
			}
		}
	}
}
