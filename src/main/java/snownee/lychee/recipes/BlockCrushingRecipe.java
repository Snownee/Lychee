package snownee.lychee.recipes;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.core.recipe.recipe.ItemShapelessRecipe;
import snownee.lychee.mixin.NonNullListAccess;
import snownee.lychee.util.RecipeMatcher;
import snownee.lychee.util.codec.CompactListCodec;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;

public class BlockCrushingRecipe extends LycheeRecipe<BlockCrushingRecipe> implements
		BlockKeyableRecipe<BlockCrushingRecipe> {
	public static final BlockPredicate ANVIL = BlockPredicate.Builder.block().of(BlockTags.ANVIL).build();

	protected BlockPredicate fallingBlock = ANVIL;
	protected @Nullable BlockPredicate landingBlock = null;
	protected NonNullList<Ingredient> ingredients = NonNullList.create();

	public BlockCrushingRecipe(final LycheeRecipeCommonProperties commonProperties) {
		super(commonProperties);
	}

	public BlockCrushingRecipe(
			final LycheeRecipeCommonProperties commonProperties,
			final BlockPredicate fallingBlock,
			@Nullable final BlockPredicate landingBlock,
			final NonNullList<Ingredient> ingredients
	) {
		super(commonProperties);
		this.fallingBlock = fallingBlock;
		this.landingBlock = landingBlock;
		this.ingredients = ingredients;
	}

	public BlockCrushingRecipe(
			final LycheeRecipeCommonProperties commonProperties,
			final BlockPredicate fallingBlock,
			@Nullable final BlockPredicate landingBlock,
			final List<Ingredient> ingredients
	) {
		super(commonProperties);
		this.fallingBlock = fallingBlock;
		this.landingBlock = landingBlock;
		this.ingredients = NonNullListAccess.construct(ingredients, null);
	}

	@Override
	public Optional<BlockPredicate> blockPredicate() {
		return Optional.ofNullable(fallingBlock);
	}

	public Optional<BlockPredicate> landingBlock() {
		return Optional.ofNullable(fallingBlock);
	}

	@Override
	public boolean matches(final LycheeContext context, final Level level) {
		final var itemShapelessContext = context.get(LycheeContextKey.ITEM_SHAPELESS);
		if (itemShapelessContext.totalItems < ingredients.size()) {
			return false;
		}
		if (!BlockPredicateExtensions.matches(landingBlock, context)) {
			return false;
		}
		final var fallingBlockEntityContext = context.get(LycheeContextKey.FALLING_BLOCK_ENTITY);
		if (!matchesFallingBlock(fallingBlockEntityContext.getBlockState(), fallingBlockEntityContext.blockData)) {
			return false;
		}
		if (ingredients.isEmpty()) {
			return true;
		}
		List<ItemEntity> itemEntities = itemShapelessContext.itemEntities.stream().filter($ -> {
			// ingredient.test is not thread safe
			return ingredients.stream().anyMatch(ingredient -> ingredient.test($.getItem()));
		}).limit(ItemShapelessRecipe.MAX_INGREDIENTS).toList();
		List<ItemStack> items = itemEntities.stream().map(ItemEntity::getItem).toList();
		int[] amount = items.stream().mapToInt(ItemStack::getCount).toArray();
		Optional<RecipeMatcher<ItemStack>> match = RecipeMatcher.findMatches(items, ingredients, amount);
		if (match.isEmpty()) {
			return false;
		}
		itemShapelessContext.filteredItems = itemEntities;
		itemShapelessContext.setMatcher(match.get());
		return true;
	}

	public boolean matchesFallingBlock(BlockState blockstate, CompoundTag nbt) {
		if (blockPredicate().isEmpty()) {
			return true;
		}
		final var blockPredicate = blockPredicate().get();
		if (blockPredicate.tag().isPresent() && !blockstate.is(blockPredicate.tag().get())) {
			return false;
		} else if (blockPredicate.blocks().isPresent() && !blockstate.is(blockPredicate.blocks().get())) {
			return false;
		} else if (blockPredicate.properties().isPresent() && !blockPredicate.properties().get().matches(blockstate)) {
			return false;
		} else {
			if (blockPredicate.nbt().isPresent()) {
				return nbt != null && blockPredicate.nbt().get().matches(nbt);
			}
			return true;
		}
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return ingredients;
	}

	@Override
	public List<BlockPredicate> getBlockInputs() {
		return List.of(fallingBlock, landingBlock);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return null;
	}

	@Override
	public @NotNull RecipeType<? extends ILycheeRecipe<BlockCrushingRecipe>> getType() {
		return null;
	}

	@Override
	public int compareTo(@NotNull final BlockCrushingRecipe that) {
		int i;
		i = Integer.compare(maxRepeats().isAny() ? 1 : 0, that.maxRepeats().isAny() ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = Integer.compare(isSpecial() ? 1 : 0, that.isSpecial() ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = Integer.compare(
				landingBlock == null ? 1 : 0,
				that.landingBlock == null ? 1 : 0
		);
		if (i != 0) {
			return i;
		}
		i = -Integer.compare(ingredients.size(), that.ingredients.size());
		return i;
	}

	public static class Serializer implements LycheeRecipeSerializer<BlockCrushingRecipe> {
		public static final Codec<BlockCrushingRecipe> CODEC =
				RecordCodecBuilder.create(instance -> instance.group(
						LycheeRecipeCommonProperties.MAP_CODEC.forGetter(BlockCrushingRecipe::commonProperties),
						BlockPredicateExtensions.CODEC.optionalFieldOf("falling_block", null).forGetter(it -> it.fallingBlock),
						BlockPredicateExtensions.CODEC.optionalFieldOf("landing_block", null).forGetter(it -> it.landingBlock),
						new CompactListCodec<>(Ingredient.CODEC_NONEMPTY).optionalFieldOf(ITEM_IN, List.of())
								.forGetter(it -> it.ingredients)
				).apply(instance, BlockCrushingRecipe::new));

		@Override
		public @NotNull Codec<BlockCrushingRecipe> codec() {
			return CODEC;
		}
	}
}
