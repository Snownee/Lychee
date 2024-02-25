package snownee.lychee;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import snownee.lychee.core.recipe.recipe.ItemShapelessRecipe;
import snownee.lychee.recipes.AnvilCraftingRecipe;
import snownee.lychee.recipes.BlockClickingRecipe;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.recipes.BlockExplodingRecipe;
import snownee.lychee.recipes.BlockInteractingRecipe;
import snownee.lychee.recipes.ItemBurningRecipe;
import snownee.lychee.recipes.ItemExplodingRecipe;
import snownee.lychee.recipes.ShapedCraftingRecipe;
import snownee.lychee.recipes.dripstone_dripping.DripstoneRecipe;
import snownee.lychee.recipes.item_inside.ItemInsideRecipe;
import snownee.lychee.recipes.lightning_channeling.LightningChannelingRecipe;
import snownee.lychee.recipes.random_block_ticking.RandomBlockTickingRecipe;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;

public final class RecipeSerializers {

	public static void init() {
	}

	public static final LycheeRecipeSerializer<ItemBurningRecipe> ITEM_BURNING = register(
			"item_burning",
			new ItemBurningRecipe.Serializer()
	);
	public static final LycheeRecipeSerializer<ItemInsideRecipe> ITEM_INSIDE = register(
			"item_inside",
			new ItemInsideRecipe.Serializer()
	);
	public static final LycheeRecipeSerializer<BlockInteractingRecipe> BLOCK_INTERACTING =
			register("block_interacting", new BlockInteractingRecipe.Serializer());
	public static final LycheeRecipeSerializer<BlockClickingRecipe> BLOCK_CLICKING = register(
			"block_clicking",
			new BlockClickingRecipe.Serializer()
	);
	public static final LycheeRecipeSerializer<AnvilCraftingRecipe> ANVIL_CRAFTING = register(
			"anvil_crafting",
			new AnvilCraftingRecipe.Serializer()
	);
	public static final LycheeRecipeSerializer<BlockCrushingRecipe> BLOCK_CRUSHING = register(
			"block_crushing",
			new BlockCrushingRecipe.Serializer()
	);
	public static final LycheeRecipeSerializer<LightningChannelingRecipe> LIGHTNING_CHANNELING = register(
			"lightning_channeling",
			new ItemShapelessRecipe.Serializer<>($ -> new LightningChannelingRecipe($))
	);
	public static final LycheeRecipeSerializer<ItemExplodingRecipe> ITEM_EXPLODING = register(
			"item_exploding",
			new ItemShapelessRecipe.Serializer<>($ -> new ItemExplodingRecipe($))
	);
	public static final LycheeRecipeSerializer<BlockExplodingRecipe> BLOCK_EXPLODING = register(
			"block_exploding",
			new BlockExplodingRecipe.Serializer()
	);
	public static final LycheeRecipeSerializer<RandomBlockTickingRecipe> RANDOM_BLOCK_TICKING = register(
			"random_block_ticking",
			new RandomBlockTickingRecipe.Serializer()
	);
	public static final LycheeRecipeSerializer<DripstoneRecipe> DRIPSTONE_DRIPPING = register(
			"dripstone_dripping",
			new DripstoneRecipe.Serializer()
	);
	public static final RecipeSerializer<ShapedCraftingRecipe> CRAFTING = register(
			"crafting",
			new ShapedCraftingRecipe.Serializer()
	);

	public static <T extends RecipeSerializer<?>> T register(String id, T t) {
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Lychee.id(id), t);
		return t;
	}

}
