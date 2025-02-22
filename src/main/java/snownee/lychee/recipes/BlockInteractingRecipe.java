package snownee.lychee.recipes;

import org.jetbrains.annotations.NotNull;

import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.BlockKeyableRecipeType;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;

public class BlockInteractingRecipe extends LycheeRecipe<LycheeContext> implements BlockKeyableRecipe<BlockInteractingRecipe> {

	public static InteractionResult invoke(
			final Player player,
			final Level level,
			final InteractionHand hand,
			final BlockHitResult hitResult) {
		if (player.isSpectator()) {
			return InteractionResult.PASS;
		}
		if (hand == InteractionHand.OFF_HAND && player.getOffhandItem().isEmpty()) {
			return InteractionResult.PASS;
		}
		if (player.getCooldowns().isOnCooldown(player.getItemInHand(hand).getItem())) {
			return InteractionResult.PASS;
		}
		final var context = new LycheeContext();
		context.put(LycheeContextKey.LEVEL, level);
		final var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		lootParamsContext.setParam(LycheeLootContextParams.DIRECTION, hitResult.getDirection());
		final var result = RecipeTypes.BLOCK_INTERACTING.process(player, hand, hitResult.getBlockPos(), hitResult.getLocation(), context);
		return result.map(it -> {
			player.swing(hand, true);
			return InteractionResult.SUCCESS;
		}).orElse(InteractionResult.PASS);
	}

	protected final Pair<Ingredient, Ingredient> input;
	protected final BlockPredicate blockPredicate;

	protected BlockInteractingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			Pair<Ingredient, Ingredient> input,
			BlockPredicate blockPredicate) {
		super(commonProperties);
		this.input = input;
		this.blockPredicate = blockPredicate;
		onConstructed();
	}

	public Pair<Ingredient, Ingredient> input() {
		return input;
	}

	@Override
	public BlockPredicate blockPredicate() {
		return blockPredicate;
	}

	@Override
	public boolean matches(LycheeContext context, Level level) {
		final var thisEntity = context.get(LycheeContextKey.LOOT_PARAMS).get(LootContextParams.THIS_ENTITY);
		final var stack = thisEntity instanceof ItemEntity itemEntity ? itemEntity.getItem() : context.getItem(0);
		return input.getFirst().test(stack) && (
				BlockPredicateExtensions.isAny(blockPredicate) || BlockPredicateExtensions.matches(blockPredicate, context)) &&
				(input.getSecond().isEmpty() || input.getSecond().test(context.getItem(1)));
	}

	@Override
	public @NotNull NonNullList<Ingredient> getIngredients() {
		return input.getSecond().isEmpty() ? NonNullList.of(Ingredient.EMPTY, input.getFirst()) : NonNullList.of(
				Ingredient.EMPTY,
				input.getFirst(),
				input.getSecond());
	}

	@Override
	public @NotNull RecipeSerializer<? extends BlockInteractingRecipe> getSerializer() {
		return RecipeSerializers.BLOCK_INTERACTING;
	}

	@Override
	public @NotNull BlockKeyableRecipeType<? extends BlockInteractingRecipe> getType() {
		return RecipeTypes.BLOCK_INTERACTING;
	}

	public static <T extends BlockInteractingRecipe> MapCodec<T> codec(Function3<LycheeRecipeCommonProperties, Pair<Ingredient, Ingredient>, BlockPredicate, T> constructor) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
						LycheeRecipeCommonProperties.mapCodec(BoundsExtensions.ONE).forGetter(T::commonProperties),
						LycheeCodecs.PAIR_INGREDIENT_CODEC.fieldOf(ITEM_IN).forGetter(T::input),
						BlockPredicateExtensions.CODEC.optionalFieldOf(BLOCK_IN, BlockPredicateExtensions.ANY).forGetter(T::blockPredicate))
				.apply(instance, constructor));
	}

	public static class Serializer implements LycheeRecipeSerializer<BlockInteractingRecipe> {
		public static MapCodec<BlockInteractingRecipe> CODEC = BlockInteractingRecipe.codec(BlockInteractingRecipe::new);

		@Override
		public @NotNull MapCodec<BlockInteractingRecipe> codec() {
			return CODEC;
		}

		public static final StreamCodec<RegistryFriendlyByteBuf, BlockInteractingRecipe> STREAM_CODEC = StreamCodec.composite(
				LycheeRecipeCommonProperties.STREAM_CODEC,
				BlockInteractingRecipe::commonProperties,
				ByteBufCodecs.fromCodecWithRegistries(LycheeCodecs.PAIR_INGREDIENT_CODEC),
				BlockInteractingRecipe::input,
				BlockPredicate.STREAM_CODEC,
				BlockInteractingRecipe::blockPredicate,
				BlockInteractingRecipe::new);

		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf, BlockInteractingRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
