package snownee.lychee.util.contextual;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.TriState;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.LycheeRecipe;

/**
 * Contains conditions
 *
 * @param <C>
 */
public interface Contextual<C extends Contextual<C>> extends RecipeCondition {
	Component SECRET_COMPONENT =
			Component.translatable("contextual.lychee.secret").withStyle(ChatFormatting.GRAY);

	List<ConditionHolder<?>> conditions();

	default int showingCount() {
		return conditions().stream()
						   .mapToInt(it -> it.condition().showingCount())
						   .sum();
	}

	default <T extends ContextualCondition<T>> void addCondition(ContextualCondition<T> condition) {
		addCondition(new ConditionHolder<>(condition));
	}

	<T extends ContextualCondition<T>> void addCondition(ConditionHolder<T> condition);

	default void appendToTooltips(
			List<Component> tooltips,
			@Nullable Level level,
			@Nullable Player player,
			int indent
	) {
		for (ConditionHolder<?> condition : conditions()) {
			if (condition.secret()) {
				TriState result = condition.condition().testForTooltips(level, player);
				ContextualConditionDisplay.appendToTooltips(tooltips, result, indent, SECRET_COMPONENT.copy());
			} else if (condition.description().isPresent()) {
				TriState result = condition.condition().testForTooltips(level, player);
				ContextualConditionDisplay.appendToTooltips(
						tooltips, result, indent, condition.description().get().copy());
			} else {
				condition.condition().appendToTooltips(tooltips, level, player, indent, false);
			}
		}
	}

	default int test(@Nullable LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		for (ConditionHolder<?> condition : conditions()) {
			try {
				times = condition.condition().test(recipe, ctx, times);
				if (times == 0) break;
			} catch (Throwable e) {
				Lychee.LOGGER.error(
						"Failed to check condition {} of recipe {}",
						LycheeRegistries.CONTEXTUAL.getKey(condition.condition().type()),
						ctx.getMatchedRecipeId(),
						e
				);
				return 0;
			}
		}
		return times;
	}
}
