package snownee.lychee.recipes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.BlockKeyableRecipeType;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;

public class BlockClickingRecipe extends BlockInteractingRecipe {

	public static InteractionResult invoke(
			final Player player,
			final Level level,
			final InteractionHand hand,
			final BlockPos pos,
			final Direction direction
	) {
		if (player.isSpectator()) {
			return InteractionResult.PASS;
		}
		final var stack = player.getItemInHand(hand);
		if (player.getCooldowns().isOnCooldown(stack.getItem())) {
			return InteractionResult.PASS;
		}
		final var vec = Vec3.atCenterOf(pos);
		final var context = new LycheeContext();
		context.put(LycheeContextKey.LEVEL, level);
		final var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		lootParamsContext.setParam(LycheeLootContextParams.DIRECTION, direction);
		final var result = RecipeTypes.BLOCK_CLICKING.process(player, hand, pos, vec, context);
		return result.map(it -> InteractionResult.SUCCESS).orElse(InteractionResult.PASS);
	}


	protected BlockClickingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			Pair<Ingredient, Ingredient> input,
			@Nullable BlockPredicate blockPredicate
	) {
		super(commonProperties, input, blockPredicate);
	}

	@Override
	public @NotNull RecipeSerializer<? extends BlockClickingRecipe> getSerializer() {
		return RecipeSerializers.BLOCK_CLICKING;
	}

	@Override
	public @NotNull BlockKeyableRecipeType<? extends BlockClickingRecipe> getType() {
		return RecipeTypes.BLOCK_CLICKING;
	}

	public static class Serializer implements LycheeRecipeSerializer<BlockClickingRecipe> {
		public static Codec<BlockClickingRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
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
				BlockPredicate.CODEC.optionalFieldOf(BLOCK_IN, null).forGetter(it -> it.blockPredicate)
		).apply(instance, BlockClickingRecipe::new));

		@Override
		public @NotNull Codec<BlockClickingRecipe> codec() {
			return CODEC;
		}
	}
}
