package snownee.lychee;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import snownee.lychee.recipes.anvil_crafting.AnvilCraftingRecipe;
import snownee.lychee.recipes.block_crushing.BlockCrushingRecipe;
import snownee.lychee.recipes.block_exploding.BlockExplodingRecipe;
import snownee.lychee.core.recipe.recipe.ItemShapelessRecipe;
import snownee.lychee.core.recipe.recipe.OldLycheeRecipe;
import snownee.lychee.recipes.crafting.ShapedCraftingRecipe;
import snownee.lychee.recipes.dripstone_dripping.DripstoneRecipe;
import snownee.lychee.recipes.interaction.BlockClickingRecipe;
import snownee.lychee.recipes.interaction.BlockInteractingRecipe;
import snownee.lychee.recipes.item_burning.ItemBurningRecipe;
import snownee.lychee.recipes.item_exploding.ItemExplodingRecipe;
import snownee.lychee.recipes.item_inside.ItemInsideRecipe;
import snownee.lychee.recipes.lightning_channeling.LightningChannelingRecipe;
import snownee.lychee.recipes.random_block_ticking.RandomBlockTickingRecipe;

public final class RecipeSerializers {

	public static void init() {
	}

	public static final OldLycheeRecipe.Serializer<ItemBurningRecipe> ITEM_BURNING = register(
			"item_burning",
			new ItemBurningRecipe.Serializer()
	);
	public static final OldLycheeRecipe.Serializer<ItemInsideRecipe> ITEM_INSIDE = register(
			"item_inside",
			new ItemInsideRecipe.Serializer()
	);
	public static final OldLycheeRecipe.Serializer<BlockInteractingRecipe> BLOCK_INTERACTING =
			register("block_interacting", new BlockInteractingRecipe.Serializer<>($ -> new BlockInteractingRecipe($)));
	public static final OldLycheeRecipe.Serializer<BlockClickingRecipe> BLOCK_CLICKING = register(
			"block_clicking",
			new BlockInteractingRecipe.Serializer<>($ -> new BlockClickingRecipe($))
	);
	public static final OldLycheeRecipe.Serializer<AnvilCraftingRecipe> ANVIL_CRAFTING = register(
			"anvil_crafting",
			new AnvilCraftingRecipe.Serializer()
	);
	public static final OldLycheeRecipe.Serializer<BlockCrushingRecipe> BLOCK_CRUSHING = register(
			"block_crushing",
			new BlockCrushingRecipe.Serializer()
	);
	public static final OldLycheeRecipe.Serializer<LightningChannelingRecipe> LIGHTNING_CHANNELING = register(
			"lightning_channeling",
			new ItemShapelessRecipe.Serializer<>($ -> new LightningChannelingRecipe($))
	);
	public static final OldLycheeRecipe.Serializer<ItemExplodingRecipe> ITEM_EXPLODING = register(
			"item_exploding",
			new ItemShapelessRecipe.Serializer<>($ -> new ItemExplodingRecipe($))
	);
	public static final OldLycheeRecipe.Serializer<BlockExplodingRecipe> BLOCK_EXPLODING = register(
			"block_exploding",
			new BlockExplodingRecipe.Serializer()
	);
	public static final OldLycheeRecipe.Serializer<RandomBlockTickingRecipe> RANDOM_BLOCK_TICKING = register(
			"random_block_ticking",
			new RandomBlockTickingRecipe.Serializer()
	);
	public static final OldLycheeRecipe.Serializer<DripstoneRecipe> DRIPSTONE_DRIPPING = register(
			"dripstone_dripping",
			new DripstoneRecipe.Serializer()
	);
	public static final RecipeSerializer<ShapedCraftingRecipe> CRAFTING = register(
			"crafting",
			new ShapedCraftingRecipe.Serializer()
	);

	public static <T extends RecipeSerializer<?>> T register(String name, T t) {
		Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, new ResourceLocation(Lychee.ID, name), t);
		return t;
	}

}
