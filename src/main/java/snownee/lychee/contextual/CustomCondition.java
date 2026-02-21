package snownee.lychee.contextual;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.kiwi.util.codec.KCodecs;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.contextual.ContextualPredicate;

/**
 * Mainly used by KubeJS script listener with `LycheeEvents.customAction('id', listener`)
 */
public class CustomCondition implements ContextualCondition {
	public final JsonObject data;
	private final String id;
	public @Nullable ContextualPredicate testFunc = null;
	public BiFunction<Level, @Nullable Player, TriState> testInTooltipsFunc = (_, _) -> TriState.DEFAULT;

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
	public int test(LycheeContext ctx, ActionContext actionContext, int times) {
		if (testFunc != null) {
			return testFunc.test(ctx, actionContext, times);
		}
		return 0;
	}

	@Override
	public TriState testForTooltips(Level level, @Nullable Player player) {
		return Objects.requireNonNull(testInTooltipsFunc.apply(level, player));
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
		// TODO 需要测试
		public static final MapCodec<CustomCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				ExtraCodecs.NON_EMPTY_STRING.fieldOf("id").forGetter(CustomCondition::id),
				ExtraCodecs.JSON.comapFlatMap(it -> KCodecs.tryCatch(it::getAsJsonObject), Function.identity())
						.optionalFieldOf("data", new JsonObject())
						.forGetter(CustomCondition::data)
		).apply(instance, CustomCondition::new));

		@Override
		public MapCodec<CustomCondition> codec() {
			return CODEC;
		}
	}
}
