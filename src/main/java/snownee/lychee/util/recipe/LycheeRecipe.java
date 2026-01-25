package snownee.lychee.util.recipe;

import net.minecraft.world.item.crafting.RecipeInput;

public abstract class LycheeRecipe<C extends RecipeInput> implements ILycheeRecipe<C> {
	protected final LycheeRecipeCommonProperties commonProperties;

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
}
