package snownee.lychee.contextual;

import java.util.List;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.contextual.Contextual;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.contextual.ContextualHolder;

public record And(ContextualHolder conditions) implements ContextualCondition, Contextual {

	@Override
	public ContextualConditionType<And> type() {
		return ContextualConditionType.AND;
	}

	@Override
	public int test(LycheeContext ctx, ActionContext actionContext, int times) {
		return conditions.test(ctx, actionContext, times);
	}

	@Override
	public TriState testForTooltips(Level level, @Nullable Player player) {
		var finalResult = TriState.TRUE;
		for (ContextualCondition condition : conditions) {
			final var result = condition.testForTooltips(level, player);
			if (result == TriState.FALSE) {
				return result;
			}
			if (result == TriState.DEFAULT) {
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
		public static final MapCodec<And> CODEC =
				RecordCodecBuilder.mapCodec(instance -> instance.group(
						ContextualHolder.CODEC
								.fieldOf("contextual")
								.forGetter(And::conditions)
				).apply(instance, And::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, And> STREAM_CODEC = ContextualHolder.STREAM_CODEC.map(
				And::new,
				And::conditions);

		@Override
		public MapCodec<And> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, And> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
