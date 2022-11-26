package snownee.lychee.core.contextual;

import java.util.Map;
import java.util.function.Predicate;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import snownee.lychee.ContextualConditionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;

public record IsWeather(String id, Predicate<Level> predicate) implements ContextualCondition {

	public static final Map<String, IsWeather> REGISTRY = Maps.newConcurrentMap();

	public static IsWeather create(String id, Predicate<Level> predicate) {
		IsWeather isWeather = new IsWeather(id, predicate);
		REGISTRY.put(id, isWeather);
		return isWeather;
	}

	public static IsWeather CLEAR = create("clear", level -> !level.isRaining() && !level.isThundering());
	public static IsWeather RAIN = create("rain", level -> level.isRaining());
	public static IsWeather THUNDER = create("thunder", level -> level.isThundering());

	@Override
	public ContextualConditionType<? extends ContextualCondition> getType() {
		return ContextualConditionTypes.WEATHER;
	}

	@Override
	public int test(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		return predicate.test(ctx.getLevel()) ? times : 0;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public InteractionResult testInTooltips() {
		return predicate.test(Minecraft.getInstance().level) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		String key = makeDescriptionId(inverted);
		MutableComponent weather = Component.translatable("weather.lychee." + id);
		return Component.translatable(key, weather.withStyle(ChatFormatting.WHITE));
	}

	public static class Type extends ContextualConditionType<IsWeather> {

		@Override
		public IsWeather fromJson(JsonObject o) {
			return REGISTRY.get(o.get("weather").getAsString());
		}

		@Override
		public void toJson(IsWeather condition, JsonObject o) {
			o.addProperty("weather", condition.id());
		}

		@Override
		public IsWeather fromNetwork(FriendlyByteBuf buf) {
			return REGISTRY.get(buf.readUtf());
		}

		@Override
		public void toNetwork(IsWeather condition, FriendlyByteBuf buf) {
			buf.writeUtf(condition.id());
		}

	}

}
