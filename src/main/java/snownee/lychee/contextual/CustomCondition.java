package snownee.lychee.contextual;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.Lychee;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.contextual.ContextualPredicate;
import snownee.lychee.util.recipe.ILycheeRecipe;

/**
 * Mainly used by KubeJS script listener with `LycheeEvents.customAction('id', listener`)
 */
public class CustomCondition implements ContextualCondition {
	public final JsonObject data;
	private final String id;
	public ContextualPredicate testFunc = null;
	public BiFunction<Level, @Nullable Player, TriState> testInTooltipsFunc = (level, player) -> TriState.DEFAULT;

	public CustomCondition(String id, JsonObject data) {
		this.id = id;
		this.data = data;
		CommonProxy.postCustomConditionEvent(GsonHelper.getAsString(data, "id"), this);
	}

	@Override
	public ContextualConditionType<CustomCondition> type() {
		return ContextualConditionType.CUSTOM;
	}

	@Override
	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		if (testFunc != null) {
			return testFunc.test(recipe, ctx, times);
		}
		return 0;
	}

	@Override
	public TriState testForTooltips(Level level, @Nullable Player player) {
		try {
			return testInTooltipsFunc.apply(level, player);
		} catch (Exception e) {
			Lychee.LOGGER.error("Error occurred while testing custom condition: {}", id, e);
			return TriState.DEFAULT;
		}
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		return Component.translatable(getDescriptionId(inverted), GsonHelper.getAsString(data, "id"));
	}

	public JsonObject data() {
		return data;
	}

	public String id() {
		return id;
	}

	public static class Type implements ContextualConditionType<CustomCondition> {
		public static final MapCodec<CustomCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				ExtraCodecs.NON_EMPTY_STRING.fieldOf("id").forGetter(CustomCondition::id),
				ExtraCodecs.JSON.comapFlatMap(
						it -> {
							try {
								return DataResult.success(it.getAsJsonObject());
							} catch (Exception e) {
								return DataResult.error(e::getMessage);
							}
						}, Function.identity()).optionalFieldOf("data", new JsonObject()).forGetter(CustomCondition::data)
		).apply(instance, CustomCondition::new));

		@Override
		public MapCodec<CustomCondition> codec() {
			return CODEC;
		}
	}
}
