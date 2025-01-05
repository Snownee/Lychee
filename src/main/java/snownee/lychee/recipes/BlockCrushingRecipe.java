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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import snownee.kiwi.util.codec.KCodecs;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.mixin.NonNullListAccess;
import snownee.lychee.util.RecipeMatcher;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.ItemShapelessRecipeUtils;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;

public class BlockCrushingRecipe extends LycheeRecipe<LycheeContext> implements BlockKeyableRecipe<BlockCrushingRecipe> {
	public static final BlockPredicate ANVIL = BlockPredicate.Builder.block().of(BlockTags.ANVIL).build();

	protected BlockPredicate fallingBlock;
	protected BlockPredicate landingBlock;
	protected NonNullList<Ingredient> ingredients;

	public BlockCrushingRecipe(
			final LycheeRecipeCommonProperties commonProperties,
			BlockPredicate fallingBlock,
			BlockPredicate landingBlock,
			final NonNullList<Ingredient> ingredients
	) {
		super(commonProperties);
		this.fallingBlock = fallingBlock;
		this.landingBlock = landingBlock;
		this.ingredients = ingredients;
		onConstructed();
	}

	public BlockCrushingRecipe(
			final LycheeRecipeCommonProperties commonProperties,
			BlockPredicate fallingBlock,
			BlockPredicate landingBlock,
			final List<Ingredient> ingredients
	) {
		this(commonProperties, fallingBlock, landingBlock, NonNullListAccess.construct(ingredients, null));
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
		if (itemShapelessContext.totalItems < ingredients.size()) {
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
		var itemEntities = itemShapelessContext.itemEntities.stream().filter($ -> {
			// ingredient.test is not thread safe
			return ingredients.stream().anyMatch(ingredient -> ingredient.test($.getItem()));
		}).limit(ItemShapelessRecipeUtils.MAX_INGREDIENTS).toList();
		var items = itemEntities.stream().map(ItemEntity::getItem).toList();
		var amount = items.stream().mapToInt(ItemStack::getCount).toArray();
		var match = RecipeMatcher.findMatches(items, ingredients, amount);
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
	public @NotNull NonNullList<Ingredient> getIngredients() {
		return ingredients;
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
						LycheeRecipeCommonProperties.MAP_CODEC.forGetter(BlockCrushingRecipe::commonProperties),
						BlockPredicateExtensions.CODEC.optionalFieldOf("falling_block", ANVIL)
								.forGetter(it -> it.fallingBlock),
						BlockPredicateExtensions.CODEC.optionalFieldOf("landing_block", BlockPredicateExtensions.ANY)
								.forGetter(BlockCrushingRecipe::landingBlock),
						KCodecs.compactList(LycheeCodecs.OPTIONAL_INGREDIENT_CODEC).optionalFieldOf(ITEM_IN, List.of())
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
						ByteBufCodecs.fromCodecWithRegistries(KCodecs.compactList(LycheeCodecs.OPTIONAL_INGREDIENT_CODEC)),
						it -> it.ingredients,
						BlockCrushingRecipe::new
				);

		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf, BlockCrushingRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
