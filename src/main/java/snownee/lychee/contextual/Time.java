package snownee.lychee.contextual;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.predicates.TimeCheck;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.core.def.NumberProviderHelper;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.TriState;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.contextual.ContextualConditionTypes;

public record Time(MinMaxBounds.Ints value, Optional<Long> period) implements ContextualCondition<Time> {

	@Override
	public ContextualConditionType<Time> type() {
		return ContextualConditionTypes.TIME;
	}

	@Override
	public int test(RecipeHolder<LycheeRecipe<?>> recipe, LycheeRecipeContext ctx, int times) {
		return test(ctx.level()) ? times : 0;
	}

	@Override
	public TriState testForTooltips(Level level, @Nullable Player player) {
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
		public static final Codec<Time> CODEC = TimeCheck.CODEC
				.xmap(it -> new Time(
							  MinMaxBounds.Ints.between(NumberProviderHelper.toConstant(it.value().min),
														NumberProviderHelper.toConstant(it.value().max)),
							  it.period()),
					  it -> {
						  final var builder = TimeCheck.time(IntRange.range(it.value().min().orElseThrow(),
																			it.value().max().orElseThrow()));
						  it.period.ifPresent(builder::setPeriod);
						  return builder.build();
					  });

		@Override
		public Codec<Time> codec() {
			return CODEC;
		}
	}
}
