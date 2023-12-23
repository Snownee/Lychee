package snownee.lychee;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.anvil_crafting.AnvilCraftingRecipe;
import snownee.lychee.block_crushing.BlockCrushingRecipe;
import snownee.lychee.block_exploding.BlockExplodingRecipe;
import snownee.lychee.core.recipe.ItemAndBlockRecipe;
import snownee.lychee.core.recipe.ItemShapelessRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.interaction.BlockClickingRecipe;
import snownee.lychee.interaction.BlockInteractingRecipe;
import snownee.lychee.item_burning.ItemBurningRecipe;
import snownee.lychee.item_exploding.ItemExplodingRecipe;
import snownee.lychee.item_inside.ItemInsideRecipe;
import snownee.lychee.lightning_channeling.LightningChannelingRecipe;

public final class RecipeSerializers {

	public static void init() {
	}

	public static final LycheeRecipe.Serializer<ItemBurningRecipe> ITEM_BURNING = register("item_burning", new ItemBurningRecipe.Serializer());
	public static final LycheeRecipe.Serializer<ItemInsideRecipe> ITEM_INSIDE = register("item_inside", new ItemInsideRecipe.Serializer());
	public static final LycheeRecipe.Serializer<BlockInteractingRecipe> BLOCK_INTERACTING = register("block_interacting", new ItemAndBlockRecipe.Serializer<>($ -> new BlockInteractingRecipe($)));
	public static final LycheeRecipe.Serializer<BlockInteractingRecipe> BLOCK_CLICKING = register("block_clicking", new ItemAndBlockRecipe.Serializer<>($ -> new BlockClickingRecipe($)));
	public static final LycheeRecipe.Serializer<AnvilCraftingRecipe> ANVIL_CRAFTING = register("anvil_crafting", new AnvilCraftingRecipe.Serializer());
	public static final LycheeRecipe.Serializer<BlockCrushingRecipe> BLOCK_CRUSHING = register("block_crushing", new BlockCrushingRecipe.Serializer());
	public static final LycheeRecipe.Serializer<LightningChannelingRecipe> LIGHTNING_CHANNELING = register("lightning_channeling", new ItemShapelessRecipe.Serializer<>($ -> new LightningChannelingRecipe($)));
	public static final LycheeRecipe.Serializer<ItemExplodingRecipe> ITEM_EXPLODING = register("item_exploding", new ItemShapelessRecipe.Serializer<>($ -> new ItemExplodingRecipe($)));
	public static final LycheeRecipe.Serializer<BlockExplodingRecipe> BLOCK_EXPLODING = register("block_exploding", new BlockExplodingRecipe.Serializer());

	public static <T extends LycheeRecipe.Serializer<?>> T register(String name, T t) {
		Registry.register(Registry.RECIPE_SERIALIZER, new ResourceLocation(Lychee.ID, name), t);
		return t;
	}

}
