package snownee.lychee.recipes;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.BlockKeyableRecipeType;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class BlockInteractingRecipe extends LycheeRecipe<LycheeContext> implements BlockKeyableRecipe<BlockInteractingRecipe> {

	public static InteractionResult invoke(
			final Player player,
			final Level level,
			final InteractionHand hand,
			final BlockHitResult hitResult) {
		if (player.isSpectator()) {
			return InteractionResult.PASS;
		}
		if (hand == InteractionHand.OFF_HAND && player.getMainHandItem().isEmpty() &&
				player.getOffhandItem().isEmpty()) {
			return InteractionResult.PASS;
		}
		if (player.getCooldowns().isOnCooldown(player.getItemInHand(hand).getItem())) {
			return InteractionResult.PASS;
		}
		final var context = new LycheeContext();
		context.put(LycheeContextKey.LEVEL, level);
		final var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		lootParamsContext.setParam(LycheeLootContextParams.DIRECTION, hitResult.getDirection());
		final var result = RecipeTypes.BLOCK_INTERACTING.process(
				player,
				hand,
				hitResult.getBlockPos(),
				hitResult.getLocation(),
				context
		);
		return result.map(it -> InteractionResult.SUCCESS).orElse(InteractionResult.PASS);
	}

	protected final Pair<Ingredient, Ingredient> input;
	protected final Optional<BlockPredicate> blockPredicate;

	protected BlockInteractingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			Pair<Ingredient, Ingredient> input,
			Optional<BlockPredicate> blockPredicate
	) {
		super(commonProperties);
		this.input = input;
		this.blockPredicate = blockPredicate;
		onConstructed();
	}

	public Pair<Ingredient, Ingredient> input() {
		return input;
	}

	@Override
	public Optional<BlockPredicate> blockPredicate() {
		return blockPredicate;
	}

	@Override
	public boolean matches(LycheeContext context, Level level) {
		final var thisEntity = context.get(LycheeContextKey.LOOT_PARAMS).get(LootContextParams.THIS_ENTITY);
		final var stack = thisEntity instanceof ItemEntity itemEntity ? itemEntity.getItem() : context.getItem(0);
		return input.getFirst().test(stack) &&
				(blockPredicate.isEmpty() || BlockPredicateExtensions.matches(blockPredicate.get(), context)) &&
				(input.getSecond().isEmpty() || input.getSecond().test(context.getItem(1)));
	}

	@Override
	public @NotNull NonNullList<Ingredient> getIngredients() {
		return input.getSecond().isEmpty() ? super.getIngredients() : NonNullList.of(Ingredient.EMPTY, input.getFirst(), input.getSecond());
	}

	@Override
	public @NotNull RecipeSerializer<? extends BlockInteractingRecipe> getSerializer() {
		return RecipeSerializers.BLOCK_INTERACTING;
	}

	@Override
	public @NotNull BlockKeyableRecipeType<? extends BlockInteractingRecipe> getType() {
		return RecipeTypes.BLOCK_INTERACTING;
	}

	@Override
	public int compareTo(BlockInteractingRecipe that) {
		int i;
		i = Integer.compare(maxRepeats().isAny() ? 1 : 0, that.maxRepeats().isAny() ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = Integer.compare(isSpecial() ? 1 : 0, that.isSpecial() ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = Integer.compare(blockPredicate == null ? 1 : 0, that.blockPredicate == null ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = Integer.compare(
				CommonProxy.isSimpleIngredient(input.getFirst()) ? 1 : 0,
				CommonProxy.isSimpleIngredient(that.input.getSecond()) ? 1 : 0
		);
		if (i != 0) {
			return i;
		}
		i = Integer.compare(
				CommonProxy.isSimpleIngredient(input.getSecond()) ? 1 : 0,
				CommonProxy.isSimpleIngredient(that.input.getSecond()) ? 1 : 0
		);
		return i;
	}

	public static class Serializer implements LycheeRecipeSerializer<BlockInteractingRecipe> {
		public static Codec<BlockInteractingRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				RecordCodecBuilder.<LycheeRecipeCommonProperties>mapCodec(commonPropertiesInstance ->
								commonPropertiesInstance.group(
										LycheeRecipeCommonProperties.HIDE_IN_VIEWER_CODEC.forGetter(LycheeRecipeCommonProperties::hideInRecipeViewer),
										LycheeRecipeCommonProperties.GHOST_CODEC.forGetter(LycheeRecipeCommonProperties::ghost),
										LycheeRecipeCommonProperties.COMMENT_CODEC.forGetter(LycheeRecipeCommonProperties::comment),
										LycheeRecipeCommonProperties.GROUP_CODEC.forGetter(LycheeRecipeCommonProperties::group),
										LycheeRecipeCommonProperties.CONTEXTUAL_CODEC.forGetter(LycheeRecipeCommonProperties::conditions),
										LycheeRecipeCommonProperties.POST_ACTION_CODEC.forGetter(LycheeRecipeCommonProperties::postActions),
										MinMaxBounds.Ints.CODEC
												.optionalFieldOf("max_repeats", BoundsExtensions.ONE)
												.forGetter(LycheeRecipeCommonProperties::maxRepeats)
								).apply(commonPropertiesInstance, LycheeRecipeCommonProperties::new))
						.forGetter(BlockInteractingRecipe::commonProperties),
				Codec.either(Codec.pair(Ingredient.CODEC, Ingredient.CODEC), Ingredient.CODEC)
						.fieldOf(ITEM_IN)
						.xmap(it -> {
							if (it.right().isPresent()) {
								return Pair.of(it.right().get(), EMPTY_INGREDIENT);
							}
							return it.left().orElseThrow();
						}, Either::left)
						.forGetter(BlockInteractingRecipe::input),
				BlockPredicate.CODEC.optionalFieldOf(BLOCK_IN).forGetter(BlockInteractingRecipe::blockPredicate)
		).apply(instance, BlockInteractingRecipe::new));

		@Override
		public @NotNull Codec<BlockInteractingRecipe> codec() {
			return CODEC;
		}
	}
}
