package snownee.lychee.item_inside;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ItemAndBlockRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;

public class ItemInsideRecipe extends ItemAndBlockRecipe<LycheeContext> {

	public ItemInsideRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeSerializers.ITEM_INSIDE;
	}

	@Override
	public LycheeRecipeType<?, ?> getType() {
		return RecipeTypes.ITEM_INSIDE;
	}

}
