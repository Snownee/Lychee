package snownee.lychee.util.recipe;

public abstract class LycheeRecipe<T extends LycheeRecipe<T>> implements ILycheeRecipe<T> {
	protected final LycheeRecipeCommonProperties commonProperties;

	protected LycheeRecipe(LycheeRecipeCommonProperties commonProperties) {
		this.commonProperties = commonProperties;
	}

	@Override
	public LycheeRecipeCommonProperties commonProperties() {
		return commonProperties;
	}
}
