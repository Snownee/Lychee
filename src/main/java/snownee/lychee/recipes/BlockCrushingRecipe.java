package snownee.lychee.recipes;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.Util;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.IngredientCollection;
import snownee.lychee.util.RecipeMatcher;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.ItemShapelessRecipeUtils;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;

public class BlockCrushingRecipe extends LycheeRecipe<LycheeContext> implements BlockKeyableRecipe {
	public static final BlockPredicate ANVIL = BlockPredicate.Builder.block().of(BlockTags.ANVIL).build();

	protected BlockPredicate fallingBlock;
	protected BlockPredicate landingBlock;
	protected IngredientCollection ingredients;

	public BlockCrushingRecipe(
			final LycheeRecipeCommonProperties commonProperties,
			BlockPredicate fallingBlock,
			BlockPredicate landingBlock,
			final IngredientCollection ingredients
	) {
		super(commonProperties);
		this.fallingBlock = fallingBlock;
		this.landingBlock = landingBlock;
		this.ingredients = ingredients;
		onConstructed();
	}

	@Override
	public BlockPredicate blockPredicate() {
		return fallingBlock;
	}

	public BlockPredicate landingBlock() {
		return landingBlock;
	}

	@Override
	public boolean matches(final LycheeContext context, final Level level) {
		final var itemShapelessContext = context.get(LycheeContextKey.ITEM_SHAPELESS);
		if (itemShapelessContext.totalItems < ingredients.ingredientCount()) {
			return false;
		}
		if (!BlockPredicateExtensions.isAny(landingBlock) && !BlockPredicateExtensions.matches(landingBlock, context)) {
			return false;
		}
		final var fallingBlockEntityContext = context.get(LycheeContextKey.FALLING_BLOCK_ENTITY);
		if (!matchesFallingBlock(fallingBlockEntityContext.getBlockState(), fallingBlockEntityContext.blockData)) {
			return false;
		}
		if (ingredients.isEmpty()) {
			return true;
		}
		var itemEntities = itemShapelessContext.itemEntities.stream().filter($ -> ingredients.anyMatch($.getItem())).limit(
				ItemShapelessRecipeUtils.MAX_INGREDIENTS).toList();
		var items = itemEntities.stream().map(ItemEntity::getItem).toList();
		var amount = items.stream().mapToInt(ItemStack::getCount).toArray();
		var match = RecipeMatcher.findMatches(items, ingredients.flattenedIngredients(), amount);
		if (match.isEmpty()) {
			return false;
		}
		itemShapelessContext.filteredItems = itemEntities;
		itemShapelessContext.setMatcher(match.get());
		return true;
	}

	public boolean matchesFallingBlock(BlockState blockstate, CompoundTag nbt) {
		if (BlockPredicateExtensions.isAny(blockPredicate())) {
			return true;
		}
		if (blockPredicate().blocks().isPresent() && !blockstate.is(blockPredicate().blocks().get())) {
			return false;
		} else if (blockPredicate().properties().isPresent() && !blockPredicate().properties().get().matches(blockstate)) {
			return false;
		} else if (blockPredicate().nbt().isEmpty()) {
			return true;
		}
		return nbt != null && blockPredicate().nbt().get().matches(nbt);
	}

	@Override
	public List<SizedIngredient> sizedIngredients() {
		return ingredients.ingredients();
	}

	@Override
	public @NotNull NonNullList<Ingredient> getIngredients() {
		return ingredients.flattenedIngredients();
	}

	@Override
	public List<BlockPredicate> getBlockInputs() {
		return Util.make(
				Lists.newArrayList(fallingBlock), it -> {
					if (!BlockPredicateExtensions.isAny(landingBlock)) {
						it.add(landingBlock);
					}
				});
	}

	@Override
	public @NotNull RecipeSerializer<BlockCrushingRecipe> getSerializer() {
		return RecipeSerializers.BLOCK_CRUSHING;
	}

	@Override
	public @NotNull RecipeType<BlockCrushingRecipe> getType() {
		return RecipeTypes.BLOCK_CRUSHING;
	}

	public static class Serializer implements LycheeRecipeSerializer<BlockCrushingRecipe> {
		public static final MapCodec<BlockCrushingRecipe> CODEC =
				ItemShapelessRecipeUtils.validatedCodec(RecordCodecBuilder.mapCodec(instance -> instance.group(
						LycheeRecipeCommonProperties.SIMPLE_MAP_CODEC.forGetter(BlockCrushingRecipe::commonProperties),
						BlockPredicateExtensions.CODEC_FOR_TESTING.optionalFieldOf("falling_block", ANVIL)
								.forGetter(it -> it.fallingBlock),
						BlockPredicateExtensions.CODEC_FOR_TESTING.optionalFieldOf("landing_block", BlockPredicateExtensions.ANY)
								.forGetter(BlockCrushingRecipe::landingBlock),
						IngredientCollection.CODEC
								.optionalFieldOf(ITEM_IN, IngredientCollection.EMPTY)
								.forGetter(it -> it.ingredients)
				).apply(instance, BlockCrushingRecipe::new)));

		@Override
		public @NotNull MapCodec<BlockCrushingRecipe> codec() {
			return CODEC;
		}

		public static final StreamCodec<RegistryFriendlyByteBuf, BlockCrushingRecipe> STREAM_CODEC =
				StreamCodec.composite(
						LycheeRecipeCommonProperties.STREAM_CODEC,
						BlockCrushingRecipe::commonProperties,
						BlockPredicate.STREAM_CODEC,
						it -> it.fallingBlock,
						BlockPredicate.STREAM_CODEC,
						BlockCrushingRecipe::landingBlock,
						IngredientCollection.STREAM_CODEC,
						it -> it.ingredients,
						BlockCrushingRecipe::new
				);

		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf, BlockCrushingRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
