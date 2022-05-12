package snownee.lychee.interaction;

import net.minecraft.resources.ResourceLocation;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.IntBoundsHelper;
import snownee.lychee.core.recipe.ItemAndBlockRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;

public class BlockInteractingRecipe extends ItemAndBlockRecipe<LycheeContext> {

	public BlockInteractingRecipe(ResourceLocation id) {
		super(id);
		maxRepeats = IntBoundsHelper.ONE;
	}

	@Override
	public LycheeRecipe.Serializer<?> getSerializer() {
		return RecipeSerializers.BLOCK_INTERACTING;
	}

	@Override
	public LycheeRecipeType<?, ?> getType() {
		return RecipeTypes.BLOCK_INTERACTING;
	}

}
