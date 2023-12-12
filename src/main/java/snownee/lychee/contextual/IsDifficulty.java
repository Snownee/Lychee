package snownee.lychee.contextual;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.TriState;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.contextual.ContextualConditionTypes;

public record IsDifficulty(List<Difficulty> difficulties) implements ContextualCondition<IsDifficulty> {

	@Override
	public ContextualConditionType<IsDifficulty> type() {
		return ContextualConditionTypes.DIFFICULTY;
	}

	@Override
	public int test(RecipeHolder<LycheeRecipe<?>> recipe, LycheeRecipeContext ctx, int times) {
		return difficulties.contains(ctx.level().getDifficulty()) ? times : 0;
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
					key, CommonProxy.white(difficulties.get(0).getKey()));
		} else {
			key += ".more";

			// TODO 需要确定显示正确
			return Component.translatable(
					key,
					CommonProxy.white(String.join(
							", ",
							difficulties.stream()
										.limit(size - 1)
										.map(Difficulty::getKey)
										.toList()
					)),
					CommonProxy.white(difficulties.get(size - 1).getKey())
			);
		}
	}

	public static class Type implements ContextualConditionType<IsDifficulty> {
		public static final Codec<IsDifficulty> CODEC =
				RecordCodecBuilder.create(instance -> instance
						.group(Codec.either(Difficulty.CODEC, Codec.list(Difficulty.CODEC))
									.xmap(
											it -> it.mapLeft(List::of).left().orElse(it.right().orElseThrow()),
											Either::right
									)
									.fieldOf("difficulty")
									.forGetter(IsDifficulty::difficulties))
						.apply(instance, IsDifficulty::new));

		@Override
		public Codec<IsDifficulty> codec() {
			return CODEC;
		}
	}
}
