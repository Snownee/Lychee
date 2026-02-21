package snownee.lychee.contextual;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.util.TriState;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;

public record Time(Holder<WorldClock> clock, MinMaxBounds.Ints value, Optional<Long> period) implements ContextualCondition {

	@Override
	public ContextualConditionType<Time> type() {
		return ContextualConditionType.TIME;
	}

	@Override
	public int test(LycheeContext ctx, ActionContext actionContext, int times) {
		return test(ctx.level()) ? times : 0;
	}

	@Override
	public TriState testForTooltips(Level level, @Nullable Player player) {
		return TriState.from(test(level));
	}

	public boolean test(Level level) {
		long time = level.clockManager().getTotalTicks(this.clock);
		if (period.isPresent()) {
			time %= period.get();
		}

		return value.matches((int) time);
	}

	public static class Type implements ContextualConditionType<Time> {
		public static final MapCodec<Time> CODEC = TimeCheck.MAP_CODEC.flatXmap(
				it -> {
					if (it.value().min == null || it.value().min.codec() != ConstantValue.MAP_CODEC) {
						return DataResult.error(() -> "`min` not exists or not a constant value");
					}
					if (it.value().max == null || it.value().max.codec() != ConstantValue.MAP_CODEC) {
						return DataResult.error(() -> "`max` not exists or not a constant value");
					}
					//noinspection DataFlowIssue
					return DataResult.success(new Time(
							it.clock(),
							MinMaxBounds.Ints.between(it.value().min.getInt(null), it.value().max.getInt(null)),
							it.period()));
				}, it -> {
					final var builder = TimeCheck.time(
							it.clock,
							IntRange.range(it.value.min().orElseThrow(), it.value.max().orElseThrow()));
					it.period.ifPresent(builder::setPeriod);
					return DataResult.success(builder.build());
				});

		@Override
		public MapCodec<Time> codec() {
			return CODEC;
		}
	}
}
