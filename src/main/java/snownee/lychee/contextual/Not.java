package snownee.lychee.contextual;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.util.TriState;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record Not(ContextualCondition condition) implements ContextualCondition {

	@Override
	public ContextualConditionType<Not> type() {
		return ContextualConditionType.NOT;
	}

	@Override
	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		return times - condition.test(recipe, ctx, times);
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
		public static final Codec<Not> CODEC = RecordCodecBuilder.create(instance -> instance
				.group(ContextualCondition.CODEC.fieldOf("contextual").forGetter(Not::condition))
				.apply(instance, Not::new));

		@Override
		public @NotNull Codec<Not> codec() {
			return CODEC;
		}
	}
}
