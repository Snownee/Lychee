package snownee.lychee.contextual;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record Time(MinMaxBounds.Ints value, Optional<Long> period) implements ContextualCondition {

	@Override
	public ContextualConditionType<Time> type() {
		return ContextualConditionType.TIME;
	}

	@Override
	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		return test(ctx.level()) ? times : 0;
	}

	@Override
	public net.fabricmc.fabric.api.util.TriState testForTooltips(Level level, @Nullable Player player) {
		return TriState.of(test(level));
	}

	public boolean test(LevelAccessor level) {
		long i = level.dayTime();
		if (period.isPresent()) {
			i %= period.get();
		}
		return value.matches((int) i);
	}

	public static class Type implements ContextualConditionType<Time> {
		public static final MapCodec<Time> CODEC = TimeCheck.CODEC.flatXmap(
				it -> {
					if (it.value().min == null || it.value().min.getType() != NumberProviders.CONSTANT) {
						return DataResult.error(() -> "`min` not exists or not a constant");
					}
					if (it.value().max == null || it.value().max.getType() != NumberProviders.CONSTANT) {
						return DataResult.error(() -> "`max` not exists or not a constant");
					}
					return DataResult.success(
							new Time(
									MinMaxBounds.Ints.between(
											it.value().min.getInt(null),
											it.value().max.getInt(null)
									),
									it.period()
							));
				},
				it -> {
					final var builder = TimeCheck.time(IntRange.range(
							it.value().min().orElseThrow(),
							it.value().max().orElseThrow()
					));
					it.period.ifPresent(builder::setPeriod);
					return DataResult.success(builder.build());
				}
		);

		@Override
		public MapCodec<Time> codec() {
			return CODEC;
		}
	}
}
