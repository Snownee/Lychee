package snownee.lychee.contextual;

import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.LootContextKeys;
import snownee.lychee.context.ActionContext;
import snownee.lychee.context.LootParamsAccess;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;

public class DirectionCheck implements ContextualCondition {
	public static final Map<String, DirectionCheck> LOOKUPS = Maps.newHashMap();

	static {
		for (final var direction : Direction.values()) {
			createLookup(
					direction.getName().toLowerCase(Locale.ENGLISH),
					ctx -> ctx.get(LootContextKeys.DIRECTION) == direction
			);
		}
		createLookup(
				"sides",
				ctx -> ctx.get(LootContextKeys.DIRECTION).getStepY() == 0
		);
		createLookup(
				"forward", ctx -> {
					final var direction = ctx.get(LootContextKeys.DIRECTION);
					final var state = ctx.get(LootContextParams.BLOCK_STATE);
					final var facing = state.getOptionalValue(BlockStateProperties.FACING)
							.or(() -> state.getOptionalValue(BlockStateProperties.HORIZONTAL_FACING))
							.or(() -> state.getOptionalValue(BlockStateProperties.VERTICAL_DIRECTION))
							.orElseThrow();
					return direction == facing;
				});
		createLookup(
				"axis", ctx -> {
					final var direction = ctx.get(LootContextKeys.DIRECTION);
					final var state = ctx.get(LootContextParams.BLOCK_STATE);
					final var axis = state.getOptionalValue(BlockStateProperties.AXIS)
							.or(() -> state.getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS))
							.orElseThrow();
					return axis.test(direction);
				});
	}

	public static void createLookup(String name, Predicate<LootParamsAccess> predicate) {
		LOOKUPS.put(name, new DirectionCheck(name, predicate));
	}

	private final String name;
	private final Predicate<LootParamsAccess> predicate;

	private DirectionCheck(String name, Predicate<LootParamsAccess> predicate) {
		this.name = name;
		this.predicate = predicate;
	}

	@Override
	public ContextualConditionType<DirectionCheck> type() {
		return ContextualConditionType.DIRECTION;
	}

	@Override
	public int test(LycheeContext ctx, ActionContext actionContext, int times) {
		return predicate.test(actionContext) ? times : 0;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		final var value = Component.translatable("direction.lychee." + name).withStyle(ChatFormatting.WHITE);
		return Component.translatable(getDescriptionId(inverted), value);
	}

	public static class Type implements ContextualConditionType<DirectionCheck> {
		// TODO 需要测试
		public static final MapCodec<DirectionCheck> CODEC = Codec.stringResolver($ -> $.name, LOOKUPS::get).fieldOf("direction");

		@Override
		public MapCodec<DirectionCheck> codec() {
			return CODEC;
		}
	}
}
