package snownee.lychee;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import snownee.lychee.recipes.AnvilCraftingRecipe;
import snownee.lychee.recipes.BlockClickingRecipe;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.recipes.BlockExplodingRecipe;
import snownee.lychee.recipes.BlockInteractingRecipe;
import snownee.lychee.recipes.DripstoneRecipe;
import snownee.lychee.recipes.EntityTickingRecipe;
import snownee.lychee.recipes.ItemBurningRecipe;
import snownee.lychee.recipes.ItemExplodingRecipe;
import snownee.lychee.recipes.ItemInsideRecipe;
import snownee.lychee.recipes.LightningChannelingRecipe;
import snownee.lychee.recipes.RandomBlockTickingRecipe;
import snownee.lychee.recipes.SculkSpreadingRecipe;
import snownee.lychee.recipes.ShapedCraftingRecipe;
import snownee.lychee.util.ui.BlankRecipe;
import snownee.lychee.util.ui.CategoryMetadata;
import snownee.lychee.util.ui.CategoryModifier;

public final class RecipeSerializers {
	public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Lychee.ID);
	public static final List<RecipeSerializer<?>> ALL = Lists.newArrayList();
	public static final RecipeSerializer<CategoryMetadata> CATEGORY_METADATA = register(
			"category_metadata",
			CategoryMetadata.CODEC,
			CategoryMetadata.STREAM_CODEC);
	public static final RecipeSerializer<CategoryModifier> CATEGORY_MODIFIER = register(
			"category_modifier",
			CategoryModifier.CODEC,
			CategoryModifier.STREAM_CODEC);
	public static final RecipeSerializer<BlankRecipe> BLANK = register(
			"blank",
			BlankRecipe.CODEC,
			BlankRecipe.STREAM_CODEC);
	public static final RecipeSerializer<ItemBurningRecipe> ITEM_BURNING = register(
			"item_burning",
			ItemBurningRecipe.CODEC,
			ItemBurningRecipe.STREAM_CODEC);
	public static final RecipeSerializer<ItemInsideRecipe> ITEM_INSIDE = register(
			"item_inside",
			ItemInsideRecipe.CODEC,
			ItemInsideRecipe.STREAM_CODEC);
	public static final RecipeSerializer<BlockInteractingRecipe> BLOCK_INTERACTING = register(
			"block_interacting",
			BlockInteractingRecipe.CODEC,
			BlockInteractingRecipe.STREAM_CODEC);
	public static final RecipeSerializer<BlockClickingRecipe> BLOCK_CLICKING = register(
			"block_clicking",
			BlockClickingRecipe.CODEC,
			BlockClickingRecipe.STREAM_CODEC);
	public static final RecipeSerializer<AnvilCraftingRecipe> ANVIL_CRAFTING = register(
			"anvil_crafting",
			AnvilCraftingRecipe.CODEC,
			AnvilCraftingRecipe.STREAM_CODEC);
	public static final RecipeSerializer<BlockCrushingRecipe> BLOCK_CRUSHING = register(
			"block_crushing",
			BlockCrushingRecipe.CODEC,
			BlockCrushingRecipe.STREAM_CODEC);
	public static final RecipeSerializer<LightningChannelingRecipe> LIGHTNING_CHANNELING = register(
			"lightning_channeling",
			LightningChannelingRecipe.CODEC,
			LightningChannelingRecipe.STREAM_CODEC);
	public static final RecipeSerializer<ItemExplodingRecipe> ITEM_EXPLODING = register(
			"item_exploding",
			ItemExplodingRecipe.CODEC,
			ItemExplodingRecipe.STREAM_CODEC);
	public static final RecipeSerializer<BlockExplodingRecipe> BLOCK_EXPLODING = register(
			"block_exploding",
			BlockExplodingRecipe.CODEC,
			BlockExplodingRecipe.STREAM_CODEC);
	public static final RecipeSerializer<RandomBlockTickingRecipe> RANDOM_BLOCK_TICKING = register(
			"random_block_ticking",
			RandomBlockTickingRecipe.CODEC,
			RandomBlockTickingRecipe.STREAM_CODEC);
	public static final RecipeSerializer<DripstoneRecipe> DRIPSTONE_DRIPPING = register(
			"dripstone_dripping",
			DripstoneRecipe.CODEC,
			DripstoneRecipe.STREAM_CODEC);
	public static final RecipeSerializer<ShapedCraftingRecipe> CRAFTING = register(
			"crafting",
			ShapedCraftingRecipe.CODEC,
			ShapedCraftingRecipe.STREAM_CODEC);
	public static final RecipeSerializer<SculkSpreadingRecipe> SCULK_SPREADING = register(
			"sculk_spreading",
			SculkSpreadingRecipe.CODEC,
			SculkSpreadingRecipe.STREAM_CODEC);
	public static final RecipeSerializer<EntityTickingRecipe> ENTITY_TICKING = register(
			"entity_ticking",
			EntityTickingRecipe.CODEC,
			EntityTickingRecipe.STREAM_CODEC);

	public static <T extends Recipe<?>> RecipeSerializer<T> register(
			String id,
			MapCodec<T> codec,
			StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
		RecipeSerializer<T> serializer = new RecipeSerializer<>(codec, streamCodec);
		ALL.add(serializer);
		RECIPE_SERIALIZERS.register(id, () -> serializer);
		return serializer;
	}

}
