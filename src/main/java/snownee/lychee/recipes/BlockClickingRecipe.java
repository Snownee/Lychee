package snownee.lychee.recipes;

import org.jetbrains.annotations.NotNull;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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
import snownee.lychee.util.codec.LycheeCodecs;
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
			BlockPredicate blockPredicate
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
		public static MapCodec<BlockClickingRecipe> CODEC = BlockInteractingRecipe.codec(BlockClickingRecipe::new);

		@Override
		public @NotNull MapCodec<BlockClickingRecipe> codec() {
			return CODEC;
		}

		public static final StreamCodec<RegistryFriendlyByteBuf, BlockClickingRecipe> STREAM_CODEC =
				StreamCodec.composite(
						LycheeRecipeCommonProperties.STREAM_CODEC,
						BlockClickingRecipe::commonProperties,
						ByteBufCodecs.fromCodecWithRegistries(LycheeCodecs.PAIR_INGREDIENT_CODEC),
						BlockClickingRecipe::input,
						BlockPredicate.STREAM_CODEC,
						BlockClickingRecipe::blockPredicate,
						BlockClickingRecipe::new
				);

		@Override
		public @NotNull StreamCodec<RegistryFriendlyByteBuf, BlockClickingRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
