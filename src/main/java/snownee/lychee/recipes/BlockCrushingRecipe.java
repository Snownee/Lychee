package snownee.lychee.recipes;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.mixin.NonNullListAccess;
import snownee.lychee.util.RecipeMatcher;
import snownee.lychee.util.codec.CompactListCodec;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.ItemShapelessRecipeUtils;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BlockCrushingRecipe extends LycheeRecipe<LycheeContext> implements BlockKeyableRecipe<BlockCrushingRecipe> {
	public static final BlockPredicate ANVIL = BlockPredicate.Builder.block().of(BlockTags.ANVIL).build();

	protected @NotNull BlockPredicate fallingBlock = ANVIL;
	protected Optional<BlockPredicate> landingBlock = Optional.empty();
	protected NonNullList<Ingredient> ingredients = NonNullList.create();

	public BlockCrushingRecipe(final LycheeRecipeCommonProperties commonProperties) {
		super(commonProperties);
		onConstructed();
	}

	public BlockCrushingRecipe(
			final LycheeRecipeCommonProperties commonProperties,
			@NotNull BlockPredicate fallingBlock,
			Optional<BlockPredicate> landingBlock,
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
			Optional<BlockPredicate> landingBlock,
			final List<Ingredient> ingredients
	) {
		super(commonProperties);
		this.fallingBlock = fallingBlock;
		this.landingBlock = landingBlock;
		this.ingredients = NonNullListAccess.construct(ingredients, null);
		onConstructed();
	}

	@Override
	public Optional<BlockPredicate> blockPredicate() {
		return Optional.of(fallingBlock);
	}

	public Optional<BlockPredicate> landingBlock() {
		return landingBlock;
	}

	@Override
	public boolean matches(final LycheeContext context, final Level level) {
		final var itemShapelessContext = context.get(LycheeContextKey.ITEM_SHAPELESS);
		if (itemShapelessContext.totalItems < ingredients.size()) {
			return false;
		}
		if (landingBlock.isPresent() && !BlockPredicateExtensions.matches(landingBlock.get(), context)) {
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
		if (blockPredicate().isEmpty()) {
			return true;
		}
		final var blockPredicate = blockPredicate().get();
		if (blockPredicate.blocks().isPresent() && !blockstate.is(blockPredicate.blocks().get())) {
			return false;
		} else if (blockPredicate.properties().isPresent() && !blockPredicate.properties().get().matches(blockstate)) {
			return false;
		} else if (blockPredicate.nbt().isEmpty()) {
			return true;
		}
		return nbt != null && blockPredicate.nbt().get().matches(nbt);
	}

	@Override
	public @NotNull NonNullList<Ingredient> getIngredients() {
		return ingredients;
	}

	@Override
	public List<BlockPredicate> getBlockInputs() {
		return List.of(fallingBlock, landingBlock.orElse(null));
	}

	@Override
	public @NotNull RecipeSerializer<BlockCrushingRecipe> getSerializer() {
		return RecipeSerializers.BLOCK_CRUSHING;
	}

	@Override
	public @NotNull RecipeType<BlockCrushingRecipe> getType() {
		return RecipeTypes.BLOCK_CRUSHING;
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
				landingBlock.isEmpty() ? 1 : 0,
				that.landingBlock.isEmpty() ? 1 : 0
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
						ExtraCodecs.strictOptionalField(BlockPredicateExtensions.CODEC, "falling_block", ANVIL)
								.forGetter(it -> it.fallingBlock),
						ExtraCodecs.strictOptionalField(BlockPredicateExtensions.CODEC, "landing_block")
								.forGetter(BlockCrushingRecipe::landingBlock),
						ExtraCodecs.strictOptionalField(new CompactListCodec<>(ExtraCodecs.withAlternative()), ITEM_IN, List.of())
								.forGetter(it -> it.ingredients)
				).apply(instance, BlockCrushingRecipe::new));

		@Override
		public @NotNull Codec<BlockCrushingRecipe> codec() {
			return CODEC;
		}
	}
}
