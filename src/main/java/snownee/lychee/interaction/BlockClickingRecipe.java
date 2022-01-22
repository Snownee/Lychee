package snownee.lychee.interaction;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;

public class BlockClickingRecipe extends BlockInteractingRecipe {

	public BlockClickingRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializers.BLOCK_CLICKING;
	}

	@Override
	public RecipeType<?> getType() {
		return RecipeTypes.BLOCK_CLICKING;
	}

}
