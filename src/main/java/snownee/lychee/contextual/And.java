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
import snownee.lychee.util.contextual.Contextual;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.contextual.ContextualHolder;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record And(ContextualHolder conditions) implements ContextualCondition, Contextual {

	@Override
	public ContextualConditionType<And> type() {
		return ContextualConditionType.AND;
	}

	@Override
	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		return conditions.test(recipe, ctx, times);
	}

	@Override
	public TriState testForTooltips(Level level, @Nullable Player player) {
		var finalResult = TriState.TRUE;
		for (ContextualCondition condition : conditions) {
			final var result = condition.testForTooltips(level, player);
			if (result == TriState.FALSE) {
				return result;
			}
			if (!result.get()) {
				finalResult = TriState.DEFAULT;
			}
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
		for (ContextualCondition condition : conditions) {
			condition.appendToTooltips(tooltips, level, player, indent + 1, false);
		}
	}

	@Override
	public int showingCount() {
		return conditions.showingCount();
	}

	public static class Type implements ContextualConditionType<And> {
		public static final Codec<And> CODEC =
				RecordCodecBuilder.create(instance -> instance
						.group(ContextualHolder.CODEC
								.fieldOf("contextual")
								.forGetter(And::conditions)
						).apply(instance, And::new));

		@Override
		public Codec<And> codec() {
			return CODEC;
		}
	}
}
