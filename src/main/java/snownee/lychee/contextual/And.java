package snownee.lychee.contextual;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.util.TriState;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.contextual.ConditionHolder;
import snownee.lychee.util.contextual.Contextual;
import snownee.lychee.util.contextual.ContextualByCommonHolder;
import snownee.lychee.util.contextual.ContextualCommonHolder;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.recipe.LycheeRecipe;

public record And(ContextualCommonHolder contextualCommonHolder) implements ContextualCondition<And>,
																			Contextual<And>,
																			ContextualByCommonHolder<And> {

	@Override
	public ContextualConditionType<And> type() {
		return ContextualConditionType.AND;
	}

	@Override
	public int test(@Nullable LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		return ContextualByCommonHolder.super.test(recipe, ctx, times);
	}

	@Override
	public TriState testForTooltips(Level level, @Nullable Player player) {
		var finalResult = TriState.TRUE;
		for (ConditionHolder<?> condition : conditions()) {
			final var result = condition.condition().testForTooltips(level, player);
			if (result == TriState.FALSE) return result;
			if (!result.get()) finalResult = TriState.DEFAULT;
		}
		return finalResult;
	}

	@Override
	public void appendToTooltips(
			List<Component> tooltips,
			Level level,
			@Nullable Player player,
			int indent,
			boolean inverted
	) {
		ContextualCondition.super.appendToTooltips(tooltips, level, player, indent, inverted);
		for (ConditionHolder<?> condition : conditions()) {
			condition.condition().appendToTooltips(tooltips, level, player, indent + 1, false);
		}
	}

	@Override
	public int showingCount() {
		return ContextualByCommonHolder.super.showingCount();
	}

	public static class Type implements ContextualConditionType<And> {
		public static final Codec<And> CODEC =
				RecordCodecBuilder.create(instance -> instance
						.group(ContextualCommonHolder.CODEC
								.fieldOf("contextual")
								.orElse(new ContextualCommonHolder())
								.forGetter(And::contextualCommonHolder)
						).apply(instance, And::new));

		@Override
		public Codec<And> codec() {
			return CODEC;
		}
	}
}
