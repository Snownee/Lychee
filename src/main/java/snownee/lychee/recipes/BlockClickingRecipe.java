package snownee.lychee.recipes;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.BlockKeyableRecipeType;
import snownee.lychee.util.recipe.LycheeRecipeCommonProperties;
import snownee.lychee.util.recipe.LycheeRecipeSerializer;

public class BlockClickingRecipe extends BlockInteractingRecipe {

	private static final int BREAK_GUARD_TTL = 10;
	private static final Map<UUID, BreakGuardRecord> breakGuard = new ConcurrentHashMap<>();

	private record BreakGuardRecord(ResourceKey<Level> dimension, BlockPos pos, long expiry) {
	}

	public static void markBreakProtected(Player player, Level level, BlockPos pos) {
		long gameTime = level.getGameTime();
		breakGuard.put(player.getUUID(), new BreakGuardRecord(level.dimension(), pos, gameTime + BREAK_GUARD_TTL));
		if (breakGuard.size() > 100) {
			breakGuard.values().removeIf(record -> record.expiry() < gameTime);
		}
	}

	public static boolean isBreakProtected(Player player, Level level, BlockPos pos) {
		var record = breakGuard.get(player.getUUID());
		if (record == null) {
			return false;
		}
		long gameTime = level.getGameTime();
		if (record.expiry() < gameTime) {
			breakGuard.remove(player.getUUID());
			return false;
		}
		if (!record.dimension().equals(level.dimension()) || !record.pos().equals(pos)) {
			return false;
		}
		markBreakProtected(player, level, pos);
		return true;
	}

	public static InteractionResult invoke(
			final Player player,
			final Level level,
			final InteractionHand hand,
			final BlockPos pos,
			final Direction direction
	) {
		if (player.isSpectator() || RecipeTypes.BLOCK_CLICKING.isEmpty()) {
			return InteractionResult.PASS;
		}
		final var stack = player.getItemInHand(hand);
		if (player.getCooldowns().isOnCooldown(stack.getItem())) {
			return InteractionResult.PASS;
		}
		final var vec = Vec3.atCenterOf(pos);
		final var context = new LycheeContext();
		context.put(LycheeContextKey.LEVEL, level);
		final var lootParams = context.initLootParams(RecipeTypes.BLOCK_CLICKING);
		lootParams.set(LycheeLootContextParams.DIRECTION, direction);
		final var result = RecipeTypes.BLOCK_CLICKING.process(player, hand, pos, vec, context);
		if (result.isPresent()) {
			if (!result.get().destroyBlock()) {
				markBreakProtected(player, level, pos);
				return InteractionResult.FAIL;
			}
			return InteractionResult.SUCCESS;
		}
		return isBreakProtected(player, level, pos) ? InteractionResult.FAIL : InteractionResult.PASS;
	}

	protected final boolean destroyBlock;

	public BlockClickingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			List<SizedIngredient> input,
			BlockPredicate blockPredicate
	) {
		this(commonProperties, input, blockPredicate, true);
	}

	public BlockClickingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			List<SizedIngredient> input,
			BlockPredicate blockPredicate,
			boolean destroyBlock
	) {
		super(commonProperties, input, blockPredicate);
		this.destroyBlock = destroyBlock;
	}

	public static BlockClickingRecipe create(BlockClickingRecipe base, boolean destroyBlock) {
		if (base.destroyBlock == destroyBlock) {
			return base;
		}
		return new BlockClickingRecipe(base.commonProperties(), base.sizedIngredients(), base.blockPredicate(), destroyBlock);
	}

	public boolean destroyBlock() {
		return destroyBlock;
	}

	@Override
	public LycheeRecipeSerializer<? extends BlockClickingRecipe> getSerializer() {
		return RecipeSerializers.BLOCK_CLICKING;
	}

	@Override
	public BlockKeyableRecipeType<? extends BlockClickingRecipe> getType() {
		return RecipeTypes.BLOCK_CLICKING;
	}

	public static class Serializer implements LycheeRecipeSerializer<BlockClickingRecipe> {
		public static MapCodec<BlockClickingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				BlockInteractingRecipe.codec(BlockClickingRecipe::new).forGetter($ -> $),
				Codec.BOOL.optionalFieldOf("destroy_block", true).forGetter(BlockClickingRecipe::destroyBlock)
		).apply(instance, BlockClickingRecipe::create));

		@Override
		public MapCodec<BlockClickingRecipe> codec() {
			return CODEC;
		}

		public static final StreamCodec<RegistryFriendlyByteBuf, BlockClickingRecipe> STREAM_CODEC =
				StreamCodec.composite(
						LycheeRecipeCommonProperties.STREAM_CODEC,
						BlockClickingRecipe::commonProperties,
						SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list(2)),
						BlockClickingRecipe::sizedIngredients,
						BlockPredicate.STREAM_CODEC,
						BlockClickingRecipe::blockPredicate,
						ByteBufCodecs.BOOL,
						BlockClickingRecipe::destroyBlock,
						BlockClickingRecipe::new
				);

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, BlockClickingRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}
}