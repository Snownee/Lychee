package snownee.lychee;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.lychee.anvil_crafting.AnvilCraftingRecipe;
import snownee.lychee.block_crushing.BlockCrushingRecipe;
import snownee.lychee.block_exploding.BlockExplodingRecipe;
import snownee.lychee.core.recipe.ItemShapelessRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.crafting.ShapedCraftingRecipe;
import snownee.lychee.dripstone_dripping.DripstoneRecipe;
import snownee.lychee.interaction.BlockClickingRecipe;
import snownee.lychee.interaction.BlockInteractingRecipe;
import snownee.lychee.item_burning.ItemBurningRecipe;
import snownee.lychee.item_exploding.ItemExplodingRecipe;
import snownee.lychee.item_inside.ItemInsideRecipe;
import snownee.lychee.lightning_channeling.LightningChannelingRecipe;
import snownee.lychee.random_block_ticking.RandomBlockTickingRecipe;

public final class RecipeSerializers {

	public static void init() {
	}

	public static final LycheeRecipe.Serializer<ItemBurningRecipe> ITEM_BURNING = register("item_burning", new ItemBurningRecipe.Serializer());
	public static final LycheeRecipe.Serializer<ItemInsideRecipe> ITEM_INSIDE = register("item_inside", new ItemInsideRecipe.Serializer());
	public static final LycheeRecipe.Serializer<BlockInteractingRecipe> BLOCK_INTERACTING = register("block_interacting", new BlockInteractingRecipe.Serializer<>(BlockInteractingRecipe::new));
	public static final LycheeRecipe.Serializer<BlockInteractingRecipe> BLOCK_CLICKING = register("block_clicking", new BlockInteractingRecipe.Serializer<>(BlockClickingRecipe::new));
	public static final LycheeRecipe.Serializer<AnvilCraftingRecipe> ANVIL_CRAFTING = register("anvil_crafting", new AnvilCraftingRecipe.Serializer());
	public static final LycheeRecipe.Serializer<BlockCrushingRecipe> BLOCK_CRUSHING = register("block_crushing", new BlockCrushingRecipe.Serializer());
	public static final LycheeRecipe.Serializer<LightningChannelingRecipe> LIGHTNING_CHANNELING = register("lightning_channeling", new ItemShapelessRecipe.Serializer<>(LightningChannelingRecipe::new));
	public static final LycheeRecipe.Serializer<ItemExplodingRecipe> ITEM_EXPLODING = register("item_exploding", new ItemShapelessRecipe.Serializer<>(ItemExplodingRecipe::new));
	public static final LycheeRecipe.Serializer<BlockExplodingRecipe> BLOCK_EXPLODING = register("block_exploding", new BlockExplodingRecipe.Serializer());
	public static final LycheeRecipe.Serializer<RandomBlockTickingRecipe> RANDOM_BLOCK_TICKING = register("random_block_ticking", new RandomBlockTickingRecipe.Serializer());
	public static final LycheeRecipe.Serializer<DripstoneRecipe> DRIPSTONE_DRIPPING = register("dripstone_dripping", new DripstoneRecipe.Serializer());
	public static final RecipeSerializer<ShapedCraftingRecipe> CRAFTING = register("crafting", new ShapedCraftingRecipe.Serializer());

	public static <T extends RecipeSerializer<?>> T register(String name, T t) {
		ForgeRegistries.RECIPE_SERIALIZERS.register(new ResourceLocation(Lychee.ID, name), t);
		return t;
	}

}
