package snownee.lychee;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.lychee.anvil_crafting.AnvilCraftingRecipe;
import snownee.lychee.core.recipe.ItemAndBlockRecipe;
import snownee.lychee.interaction.BlockClickingRecipe;
import snownee.lychee.interaction.BlockInteractingRecipe;
import snownee.lychee.item_burning.ItemBurningRecipe;
import snownee.lychee.item_inside.ItemInsideRecipe;

public final class RecipeSerializers {

	public static void init() {
	}

	public static final RecipeSerializer<ItemBurningRecipe> ITEM_BURNING = register("item_burning", new ItemBurningRecipe.Serializer());
	public static final RecipeSerializer<ItemInsideRecipe> ITEM_INSIDE = register("item_inside", new ItemInsideRecipe.Serializer());
	public static final RecipeSerializer<BlockInteractingRecipe> BLOCK_INTERACTING = register("block_interacting", new ItemAndBlockRecipe.Serializer<>(BlockInteractingRecipe::new));
	public static final RecipeSerializer<BlockInteractingRecipe> BLOCK_CLICKING = register("block_clicking", new ItemAndBlockRecipe.Serializer<>(BlockClickingRecipe::new));
	public static final RecipeSerializer<AnvilCraftingRecipe> ANVIL_CRAFTING = register("anvil_crafting", new AnvilCraftingRecipe.Serializer());

	public static <T extends RecipeSerializer<?>> T register(String name, T t) {
		ForgeRegistries.RECIPE_SERIALIZERS.register(t.setRegistryName(new ResourceLocation(Lychee.ID, name)));
		return t;
	}

}
