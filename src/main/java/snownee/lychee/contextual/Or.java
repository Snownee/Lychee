package snownee.lychee.contextual;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.contextual.Contextual;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.contextual.ContextualHolder;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record Or(ContextualHolder conditions) implements ContextualCondition, Contextual {
	@Override
	public ContextualConditionType<Or> type() {
		return ContextualConditionType.OR;
	}

	@Override
	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		for (ContextualCondition condition : conditions) {
			int result = condition.test(recipe, ctx, times);
			if (result > 0) {
				return result;
			}
		}
		return 0;
	}

	@Override
	public TriState testForTooltips(Level level, @Nullable Player player) {
		boolean allFailed = true;
		for (ContextualCondition condition : conditions) {
			TriState result = condition.testForTooltips(level, player);
			if (result == TriState.TRUE) {
				return result;
			}
			if (result != TriState.FALSE) {
				allFailed = false;
			}
		}
		return allFailed ? TriState.FALSE : TriState.DEFAULT;
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

	public static class Type implements ContextualConditionType<Or> {
		public static final MapCodec<Or> CODEC =
				RecordCodecBuilder.mapCodec(instance -> instance
						.group(ContextualHolder.CODEC
								.fieldOf("contextual")
								.forGetter(Or::conditions)
						).apply(instance, Or::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, Or> STREAM_CODEC = ContextualHolder.STREAM_CODEC.map(
				Or::new,
				Or::conditions);

		@Override
		public MapCodec<Or> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, Or> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
