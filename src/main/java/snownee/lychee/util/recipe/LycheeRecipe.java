package snownee.lychee.util.recipe;

import net.minecraft.world.Container;

public abstract class LycheeRecipe<C extends Container> implements ILycheeRecipe<C> {
	protected final LycheeRecipeCommonProperties commonProperties;

	protected LycheeRecipe(LycheeRecipeCommonProperties commonProperties) {
		this.commonProperties = commonProperties;
	}

	@Override
	public LycheeRecipeCommonProperties commonProperties() {
		return commonProperties;
	}
}
