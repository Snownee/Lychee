package snownee.lychee.recipes;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.function.IntFunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.kiwi.util.codec.KCodecs;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
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
			final Direction direction,
			Action action
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
		context.put(LycheeContextKey.CLICK_ACTION, action);
		final var lootParams = context.initLootParams(RecipeTypes.BLOCK_CLICKING);
		lootParams.set(LycheeLootContextParams.DIRECTION, direction);
		final var result = RecipeTypes.BLOCK_CLICKING.process(player, hand, pos, vec, context);
		if (result.isEmpty() || result.get().canDestroy()) {
			return InteractionResult.PASS;
		}
		return InteractionResult.SUCCESS;
	}

	protected final boolean canDestroy;
	protected final EnumSet<Action> actions;

	public BlockClickingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			List<SizedIngredient> input,
			BlockPredicate blockPredicate
	) {
		this(commonProperties, input, blockPredicate, true, EnumSet.of(Action.START));
	}

	public BlockClickingRecipe(
			LycheeRecipeCommonProperties commonProperties,
			List<SizedIngredient> input,
			BlockPredicate blockPredicate,
			boolean canDestroy,
			EnumSet<Action> actions
	) {
		super(commonProperties, input, blockPredicate);
		this.canDestroy = canDestroy;
		this.actions = actions;
	}

	public static BlockClickingRecipe create(BlockClickingRecipe base, boolean canDestroy, EnumSet<Action> actions) {
		if (base.canDestroy == canDestroy && base.actions.equals(actions)) {
			return base;
		}
		return new BlockClickingRecipe(base.commonProperties(), base.sizedIngredients(), base.blockPredicate(), canDestroy, actions);
	}

	@Override
	public boolean matches(LycheeContext context, Level level) {
		return actions.contains(context.getOrNull(LycheeContextKey.CLICK_ACTION)) && super.matches(context, level);
	}

	public boolean canDestroy() {
		return canDestroy;
	}

	public EnumSet<Action> actions() {
		return actions;
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
				Codec.BOOL.optionalFieldOf("can_destroy", false).forGetter(BlockClickingRecipe::canDestroy),
				Action.ACTIONS_CODEC.optionalFieldOf("action", EnumSet.of(Action.START)).forGetter(BlockClickingRecipe::actions)
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
						BlockClickingRecipe::canDestroy,
						Action.ACTIONS_STREAM_CODEC,
						BlockClickingRecipe::actions,
						BlockClickingRecipe::new
				);

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, BlockClickingRecipe> streamCodec() {
			return STREAM_CODEC;
		}
	}

	public enum Action implements StringRepresentable {
		START,
		STOP,
		ABORT;

		private static final IntFunction<Action> BY_ID = ByIdMap.continuous(Action::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
		public static final StreamCodec<ByteBuf, Action> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Action::ordinal);
		public static final StreamCodec<ByteBuf, EnumSet<Action>> ACTIONS_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list())
				.map(EnumSet::copyOf, List::copyOf);
		public static final Codec<Action> CODEC = StringRepresentable.fromEnum(Action::values);
		public static final Codec<EnumSet<Action>> ACTIONS_CODEC = KCodecs.compactList(CODEC).xmap(EnumSet::copyOf, List::copyOf);

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ENGLISH);
		}
	}
}