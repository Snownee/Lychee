package snownee.lychee.util.contextual;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.TriState;
import snownee.lychee.util.codec.CompactListCodec;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record ContextualContainer(List<ConditionHolder<?>> conditions) implements ContextualPredicate, Iterable<ConditionHolder<?>> {
	public static final Component SECRET_COMPONENT = Component.translatable("contextual.lychee.secret").withStyle(ChatFormatting.GRAY);
	public static final Codec<ContextualContainer> CODEC = new CompactListCodec<>(ConditionHolder.CODEC)
			.xmap(ContextualContainer::of, ContextualContainer::conditions);
	public static final ContextualContainer EMPTY = new ContextualContainer(List.of());

	private static ContextualContainer of(List<ConditionHolder<?>> holders) {
		if (holders.isEmpty()) {
			return EMPTY;
		} else {
			return new ContextualContainer(holders);
		}
	}

	@Override
	public List<ConditionHolder<?>> conditions() {
		return Collections.unmodifiableList(conditions);
	}

	public int showingCount() {
		return conditions().stream().mapToInt(it -> it.condition().showingCount()).sum();
	}

	public void appendToTooltips(List<Component> tooltips, @Nullable Level level, @Nullable Player player, int indent) {
		for (ConditionHolder<?> condition : conditions) {
			if (condition.secret()) {
				TriState result = condition.condition().testForTooltips(level, player);
				ContextualConditionDisplay.appendToTooltips(tooltips, result, indent, SECRET_COMPONENT.copy());
			} else if (condition.description().isPresent()) {
				TriState result = condition.condition().testForTooltips(level, player);
				ContextualConditionDisplay.appendToTooltips(tooltips, result, indent, condition.description().get().copy());
			} else {
				condition.condition().appendToTooltips(tooltips, level, player, indent, false);
			}
		}
	}

	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		for (ConditionHolder<?> condition : conditions) {
			try {
				times = condition.condition().test(recipe, ctx, times);
				if (times == 0) break;
			} catch (Throwable e) {
				Lychee.LOGGER.error("Failed to check condition {} of recipe {}", LycheeRegistries.CONTEXTUAL.getKey(condition.condition().type()), ctx.getMatchedRecipeId(), e);
				return 0;
			}
		}
		return times;
	}

	@NotNull
	@Override
	public Iterator<ConditionHolder<?>> iterator() {
		return conditions.iterator();
	}
}
