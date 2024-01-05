package snownee.lychee.contextual;

import java.util.Map;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.core.recipe.recipe.OldLycheeRecipe;
import snownee.lychee.util.TriState;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.contextual.ContextualConditionTypes;

public record IsWeather(String id, Predicate<Level> predicate) implements ContextualCondition<IsWeather> {

	public static final Map<String, IsWeather> REGISTRY = Maps.newConcurrentMap();

	public static IsWeather create(String id, Predicate<Level> predicate) {
		IsWeather isWeather = new IsWeather(id, predicate);
		REGISTRY.put(id, isWeather);
		return isWeather;
	}

	public static IsWeather CLEAR = create("clear", level -> !level.isRaining() && !level.isThundering());
	public static IsWeather RAIN = create("rain", Level::isRaining);
	public static IsWeather THUNDER = create("thunder", Level::isThundering);

	@Override
	public ContextualConditionType<IsWeather> type() {
		return ContextualConditionTypes.WEATHER;
	}

	@Override
	public int test(RecipeHolder<OldLycheeRecipe<?>> recipe, LycheeRecipeContext ctx, int times) {
		return predicate.test(ctx.level()) ? times : 0;
	}

	@Override
	public TriState testForTooltips(Level level, @Nullable Player player) {
		return TriState.of(predicate.test(level));
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		String key = getDescriptionId(inverted);
		MutableComponent weather = Component.translatable("weather.lychee." + id);
		return Component.translatable(key, weather.withStyle(ChatFormatting.WHITE));
	}

	public static class Type implements ContextualConditionType<IsWeather> {
		public static final Codec<IsWeather> CODEC =
				RecordCodecBuilder.create(instance -> instance
						.group(Codec.STRING.fieldOf("weather").forGetter(IsWeather::id))
						.apply(instance, REGISTRY::get));

		@Override
		public Codec<IsWeather> codec() {
			return CODEC;
		}
	}
}
