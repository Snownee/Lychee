package snownee.lychee.recipes.interaction;

import org.jetbrains.annotations.NotNull;

import net.minecraft.resources.ResourceLocation;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.recipe.OldLycheeRecipe;
import snownee.lychee.util.recipe.type.LycheeRecipeType;

public class BlockClickingRecipe extends BlockInteractingRecipe {

	public BlockClickingRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public OldLycheeRecipe.@NotNull Serializer<?> getSerializer() {
		return RecipeSerializers.BLOCK_CLICKING;
	}

	@Override
	public @NotNull LycheeRecipeType<?, ?> getType() {
		return RecipeTypes.BLOCK_CLICKING;
	}

}
