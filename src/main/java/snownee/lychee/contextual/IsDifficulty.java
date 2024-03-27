package snownee.lychee.contextual;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.util.TriState;
import snownee.lychee.util.codec.CompactListCodec;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record IsDifficulty(List<Difficulty> difficulties) implements ContextualCondition {

	@Override
	public ContextualConditionType<IsDifficulty> type() {
		return ContextualConditionType.DIFFICULTY;
	}

	@Override
	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		return difficulties.contains(ctx.get(LycheeContextKey.LEVEL).getDifficulty()) ? times : 0;
	}

	@Override
	public TriState testForTooltips(Level level, @Nullable Player player) {
		return TriState.of(difficulties.contains(level.getDifficulty()));
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		String key = getDescriptionId(inverted);
		int size = difficulties.size();
		if (size == 1) {
			return Component.translatable(
					key, difficulties.get(0).getDisplayName().copy().withStyle(ChatFormatting.WHITE));
		} else {
			key += ".more";

			var components = difficulties.stream()
					.map(it -> it.getDisplayName().copy().withStyle(ChatFormatting.WHITE))
					.toList();

			var component = Component.empty().append(components.get(0));

			for (int i = 1; i < size - 1; i++) {
				component.append(", ");
				component.append(components.get(i));
			}

			return Component.translatable(
					key,
					component,
					components.get(size - 1)
			);
		}
	}

	public static class Type implements ContextualConditionType<IsDifficulty> {
		public static final Codec<Difficulty> DIFFICULTY_CODEC = ExtraCodecs.withAlternative(
				Difficulty.CODEC,
				ExtraCodecs.NON_NEGATIVE_INT.xmap(Difficulty::byId, Difficulty::getId));

		public static final Codec<IsDifficulty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				new CompactListCodec<>(DIFFICULTY_CODEC, true).fieldOf("difficulty").forGetter(IsDifficulty::difficulties)
		).apply(instance, IsDifficulty::new));

		@Override
		public @NotNull Codec<IsDifficulty> codec() {
			return CODEC;
		}
	}
}
