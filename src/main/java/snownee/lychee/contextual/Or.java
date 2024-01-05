package snownee.lychee.contextual;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.util.TriState;
import snownee.lychee.util.contextual.ConditionHolder;
import snownee.lychee.util.contextual.Contextual;
import snownee.lychee.util.contextual.ContextualByConditionsHolder;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.contextual.ContextualConditionTypes;
import snownee.lychee.util.contextual.ContextualConditionsHolder;
import snownee.lychee.core.recipe.recipe.OldLycheeRecipe;

public record Or(ContextualConditionsHolder conditionsHolder) implements ContextualCondition<Or>,
																		 Contextual<Or>,
																		 ContextualByConditionsHolder<Or> {
	@Override
	public ContextualConditionType<Or> type() {
		return ContextualConditionTypes.OR;
	}

	@Override
	public int test(RecipeHolder<OldLycheeRecipe<?>> recipe, LycheeRecipeContext ctx, int times) {
		for (ConditionHolder<?> condition : conditions()) {
			int result = condition.condition().test(recipe, ctx, times);
			if (result > 0) {
				return result;
			}
		}
		return 0;
	}

	@Override
	public Codec<Or> codec() {
		return type().codec();
	}

	@Override
	public TriState testForTooltips(Level level, @Nullable Player player) {
		boolean allFailed = true;
		for (ConditionHolder<?> condition : conditions()) {
			TriState result = condition.condition().testForTooltips(level, player);
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
		for (ConditionHolder<?> condition : conditions()) {
			condition.condition().appendToTooltips(tooltips, level, player, indent + 1, false);
		}
	}

	@Override
	public int showingCount() {
		return ContextualByConditionsHolder.super.showingCount();
	}

	public static class Type implements ContextualConditionType<Or> {
		public static final Codec<Or> CODEC =
				RecordCodecBuilder.create(instance -> instance
						.group(ContextualConditionsHolder.CODEC
									   .fieldOf("contextual")
									   .orElse(new ContextualConditionsHolder())
									   .forGetter(Or::conditionsHolder)
						).apply(instance, Or::new));

		@Override
		public Codec<Or> codec() {
			return CODEC;
		}
	}
}
