package snownee.lychee.recipes;

import java.util.List;
import java.util.Optional;

import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.lychee.LootContextKeys;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipe;
import snownee.lychee.util.recipe.BlockKeyableRecipeType;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;

public class BlockInteractingRecipe extends LycheeRecipe<LycheeContext> implements BlockKeyableRecipe {

	public static InteractionResult invoke(
			final Player player,
			final Level level,
			final InteractionHand hand,
			final BlockHitResult hitResult) {
		if (player.isSpectator() || RecipeTypes.BLOCK_INTERACTING.isEmpty()) {
			return InteractionResult.PASS;
		}
		if (hand == InteractionHand.OFF_HAND && player.getOffhandItem().isEmpty()) {
			return InteractionResult.PASS;
		}
		if (player.getCooldowns().isOnCooldown(player.getItemInHand(hand))) {
			return InteractionResult.PASS;
		}
		final var context = new LycheeContext();
		context.put(LycheeContextKey.LEVEL, level);
		final var lootParams = context.initLootParams(RecipeTypes.BLOCK_INTERACTING);
		lootParams.set(LootContextKeys.DIRECTION, hitResult.getDirection());
		final var result = RecipeTypes.BLOCK_INTERACTING.process(player, hand, hitResult.getBlockPos(), hitResult.getLocation(), context);
		if (result.isPresent()) {
			player.swing(hand, true);
			return InteractionResult.SUCCESS;
		} else {
			return InteractionResult.PASS;
		}
	}

	protected final List<Optional<SizedIngredient>> inputs;
	protected final BlockPredicate blockPredicate;

	public BlockInteractingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			List<Optional<SizedIngredient>> inputs,
			BlockPredicate blockPredicate) {
		super(commonProperties);
		this.inputs = inputs;
		this.blockPredicate = blockPredicate;
		onConstructed();
	}

	public static <T extends BlockInteractingRecipe> MapCodec<T> codec(Function3<LycheeRecipeCommonProperties, List<Optional<SizedIngredient>>, BlockPredicate, T> constructor) {
		return RecordCodecBuilder.mapCodec(instance -> instance.group(
				LycheeRecipeCommonProperties.mapCodec(BoundsExtensions.ONE).forGetter(T::commonProperties),
				LycheeCodecs.sizeLimit(ExtraCodecs.compactListCodec(ExtraCodecs.optionalEmptyMap(LycheeCodecs.SIZED_INGREDIENT)), 1, 2)
						.fieldOf(ITEM_IN)
						.forGetter(T::inputs),
				BlockPredicateExtensions.CODEC_FOR_TESTING.optionalFieldOf(BLOCK_IN, BlockPredicateExtensions.ANY)
						.forGetter(T::blockPredicate)).apply(instance, constructor));
	}

	public List<Optional<SizedIngredient>> inputs() {
		return inputs;
	}

	@Override
	public List<SizedIngredient> sizedIngredients() {
		return inputs.stream().filter(Optional::isPresent).map(Optional::get).toList();
	}

	@Override
	public BlockPredicate blockPredicate() {
		return blockPredicate;
	}

	@Override
	public boolean matches(LycheeContext context, Level level) {
		return matchesInput(inputs.getFirst(), context.getItem(0)) &&
				(inputs.size() == 1 || matchesInput(inputs.getLast(), context.getItem(1))) &&
				(BlockPredicateExtensions.isAny(blockPredicate) || BlockPredicateExtensions.matches(blockPredicate, context));
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public static boolean matchesInput(Optional<SizedIngredient> ingredient, ItemStack itemStack) {
		return ingredient.map($ -> $.test(itemStack)).orElseGet(itemStack::isEmpty);
	}

	@Override
	public List<Ingredient> getIngredients() {
		return sizedIngredients().stream().map(SizedIngredient::ingredient).toList();
	}

	@Override
	public RecipeSerializer<? extends ILycheeRecipe<LycheeContext>> getSerializer() {
		return RecipeSerializers.BLOCK_INTERACTING;
	}

	@Override
	public BlockKeyableRecipeType<? extends BlockInteractingRecipe> getType() {
		return RecipeTypes.BLOCK_INTERACTING;
	}

	public static MapCodec<BlockInteractingRecipe> CODEC = BlockInteractingRecipe.codec(BlockInteractingRecipe::new);

	public static final StreamCodec<RegistryFriendlyByteBuf, BlockInteractingRecipe> STREAM_CODEC = StreamCodec.composite(
			LycheeRecipeCommonProperties.STREAM_CODEC,
			BlockInteractingRecipe::commonProperties,
			SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs::optional).apply(ByteBufCodecs.list(2)),
			BlockInteractingRecipe::inputs,
			BlockPredicate.STREAM_CODEC,
			BlockInteractingRecipe::blockPredicate,
			BlockInteractingRecipe::new);
}
