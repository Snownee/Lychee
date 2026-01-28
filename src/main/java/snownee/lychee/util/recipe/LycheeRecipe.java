package snownee.lychee.util.recipe;

import net.minecraft.world.item.crafting.RecipeInput;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.lychee.util.IngredientCollection;

public abstract class LycheeRecipe<C extends RecipeInput> implements ILycheeRecipe<C> {
	protected final LycheeRecipeCommonProperties commonProperties;
	private int ingredientCount = -1;

	protected LycheeRecipe(LycheeRecipeCommonProperties commonProperties) {
		this.commonProperties = commonProperties;
	}

	@Override
	public LycheeRecipeCommonProperties commonProperties() {
		return commonProperties;
	}

	@Override
	public abstract LycheeRecipeType<? extends ILycheeRecipe<C>> getType();

	@Override
	public abstract LycheeRecipeSerializer<? extends ILycheeRecipe<C>> getSerializer();

	@Override
	public final int ingredientCount() {
		if (ingredientCount == -1) {
			IngredientCollection ingredientCollection = ingredientCollection();
			if (ingredientCollection != null) {
				ingredientCount = ingredientCollection.ingredientCount();
			} else {
				ingredientCount = sizedIngredients().stream().mapToInt(SizedIngredient::count).sum();
			}
		}
		return ingredientCount;
	}
}
