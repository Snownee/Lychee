package snownee.lychee.contextual;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;

public record Not(ContextualCondition condition) implements ContextualCondition {

	@Override
	public ContextualConditionType<Not> type() {
		return ContextualConditionType.NOT;
	}

	@Override
	public int test(LycheeContext ctx, ActionContext actionContext, int times) {
		return times - condition.test(ctx, actionContext, times);
	}

	@Override
	public TriState testForTooltips(Level level, @Nullable Player player) {
		return switch (condition.testForTooltips(level, player)) {
			case TRUE -> TriState.FALSE;
			case FALSE -> TriState.TRUE;
			default -> TriState.DEFAULT;
		};
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		return condition.getDescription(!inverted);
	}

	public static class Type implements ContextualConditionType<Not> {
		public static final MapCodec<Not> CODEC = ContextualCondition.CODEC.xmap(Not::new, Not::condition).fieldOf("contextual");
		public static final StreamCodec<RegistryFriendlyByteBuf, Not> STREAM_CODEC = ContextualCondition.STREAM_CODEC.map(
				Not::new,
				Not::condition);

		@Override
		public MapCodec<Not> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, Not> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
