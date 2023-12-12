package snownee.lychee.contextual;

import java.util.List;

import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.TriState;
import snownee.lychee.util.contextual.ConditionHolder;
import snownee.lychee.util.contextual.Contextual;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.contextual.ContextualConditionTypes;

public record And(List<ConditionHolder<?>> conditions) implements ContextualCondition<And>, Contextual<And> {
	public And() {
		this(Lists.newArrayList());
	}

	@Override
	public List<ConditionHolder<?>> conditions() {
		return ImmutableList.copyOf(conditions);
	}

	@Override
	public <T extends ContextualCondition<T>> void addCondition(ConditionHolder<T> condition) {
		conditions.add(condition);
	}

	@Override
	public ContextualConditionType<And> type() {
		return ContextualConditionTypes.AND;
	}

	@Override
	public int test(RecipeHolder<LycheeRecipe<?>> recipe, LycheeRecipeContext ctx, int times) {
		return Contextual.super.test(recipe, ctx, times);
	}

	@Override
	public Codec<And> codec() {
		return type().codec();
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
			List<Component> tooltips, Level level, @Nullable Player player, int indent, boolean inverted
	) {
		ContextualCondition.super.appendToTooltips(tooltips, level, player, indent, inverted);
		for (ConditionHolder<?> condition : conditions()) {
			condition.condition().appendToTooltips(tooltips, level, player, indent + 1, false);
		}
	}

	@Override
	public int showingCount() {
		return Contextual.super.showingCount();
	}

	public static class Type implements ContextualConditionType<And> {
		public static final Codec<And> CODEC =
				RecordCodecBuilder.create(instance -> instance
						.group(Codec.list(ConditionHolder.CODEC)
									.fieldOf("contextual")
									.orElse(Lists.newArrayList())
									.forGetter(Contextual::conditions)
						).apply(instance, And::new));

		@Override
		public Codec<And> codec() {
			return CODEC;
		}
	}
}
