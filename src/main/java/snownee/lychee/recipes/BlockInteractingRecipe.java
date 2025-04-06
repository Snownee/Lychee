package snownee.lychee.recipes;

import java.util.List;
import java.util.Optional;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.kiwi.util.codec.KCodecs;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.NonNullListExtensions;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.BlockKeyableRecipeType;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;

public class BlockInteractingRecipe extends LycheeRecipe<LycheeContext> implements BlockKeyableRecipe {

	private static final Codec<SizedIngredient> OPTIONAL_SIZED_INGREDIENT_CODEC = ExtraCodecs.optionalEmptyMap(SizedIngredient.CODEC).xmap(
			it -> it.orElse(SizedIngredient.EMPTY),
			Optional::of);

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

	protected final List<SizedIngredient> input;
	protected final BlockPredicate blockPredicate;

	public BlockInteractingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			List<SizedIngredient> input,
			BlockPredicate blockPredicate) {
		super(commonProperties);
		this.input = input;
		this.blockPredicate = blockPredicate;
		onConstructed();
	}

	public static <T extends BlockInteractingRecipe> MapCodec<T> codec(Function3<LycheeRecipeCommonProperties, List<SizedIngredient>, BlockPredicate, T> constructor) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
						LycheeRecipeCommonProperties.mapCodec(BoundsExtensions.ONE).forGetter(T::commonProperties),
						LycheeCodecs.sizeLimit(KCodecs.compactList(OPTIONAL_SIZED_INGREDIENT_CODEC), 1, 2)
								.fieldOf(ITEM_IN)
								.forGetter(T::sizedIngredients),
						BlockPredicateExtensions.CODEC_FOR_TESTING.optionalFieldOf(BLOCK_IN, BlockPredicateExtensions.ANY).forGetter(T::blockPredicate))
				.apply(instance, constructor));
	}

	@Override
	public List<SizedIngredient> sizedIngredients() {
		return input;
	}

	@Override
	public BlockPredicate blockPredicate() {
		return blockPredicate;
	}

	@Override
	public boolean matches(LycheeContext context, Level level) {
		return input.getFirst().test(context.getItem(0)) && (
				BlockPredicateExtensions.isAny(blockPredicate) || BlockPredicateExtensions.matches(blockPredicate, context)) &&
				(input.size() == 1 || input.getLast().test(context.getItem(1)));
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return NonNullListExtensions.copyOf(input.stream().map(SizedIngredient::ingredient).toList());
	}

	@Override
	public LycheeRecipeSerializer<? extends BlockInteractingRecipe> getSerializer() {
		return RecipeSerializers.BLOCK_INTERACTING;
	}

	@Override
	public BlockKeyableRecipeType<? extends BlockInteractingRecipe> getType() {
		return RecipeTypes.BLOCK_INTERACTING;
	}

	public static class Serializer implements LycheeRecipeSerializer<BlockInteractingRecipe> {
		public static MapCodec<BlockInteractingRecipe> CODEC = BlockInteractingRecipe.codec(BlockInteractingRecipe::new);

		@Override
		public MapCodec<BlockInteractingRecipe> codec() {
			return CODEC;
		}

		public static final StreamCodec<RegistryFriendlyByteBuf, BlockInteractingRecipe> STREAM_CODEC = StreamCodec.composite(
				LycheeRecipeCommonProperties.STREAM_CODEC,
				BlockInteractingRecipe::commonProperties,
				SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list(2)),
				BlockInteractingRecipe::sizedIngredients,
				BlockPredicate.STREAM_CODEC,
				BlockInteractingRecipe::blockPredicate,
				BlockInteractingRecipe::new);

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, BlockInteractingRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
