package snownee.lychee.contextual;

import java.util.Map;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record IsWeather(String id, Predicate<Level> predicate) implements ContextualCondition {

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
		return ContextualConditionType.WEATHER;
	}

	@Override
	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		return predicate.test(ctx.level()) ? times : 0;
	}

	@Override
	public net.fabricmc.fabric.api.util.TriState testForTooltips(Level level, @Nullable Player player) {
		return TriState.of(predicate.test(level));
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		String key = getDescriptionId(inverted);
		MutableComponent weather = Component.translatable("weather.lychee." + id);
		return Component.translatable(key, weather.withStyle(ChatFormatting.WHITE));
	}

	public static class Type implements ContextualConditionType<IsWeather> {
		public static final MapCodec<IsWeather> CODEC = Codec.stringResolver(IsWeather::id, IsWeather.REGISTRY::get).fieldOf("weather");
		public static final StreamCodec<ByteBuf, IsWeather> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(
				IsWeather.REGISTRY::get,
				IsWeather::id);

		@Override
		public MapCodec<IsWeather> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, IsWeather> streamCodec() {
			return STREAM_CODEC.cast();
		}
	}
}
