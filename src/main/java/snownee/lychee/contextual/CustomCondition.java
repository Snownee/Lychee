package snownee.lychee.contextual;

import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.KeyDispatchCodec;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.TriState;
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
	public BiFunction<Level, @Nullable Player, InteractionResult> testInTooltipsFunc =
			(level, player) -> InteractionResult.PASS;

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
		return switch (testInTooltipsFunc.apply(level, player)) {
			case SUCCESS -> TriState.TRUE;
			case FAIL -> TriState.FALSE;
			case PASS, CONSUME_PARTIAL, CONSUME -> TriState.DEFAULT;
		};
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		return Component.translatable(getDescriptionId(inverted), GsonHelper.getAsString(data, "id"));
	}

	public static class Type implements ContextualConditionType<CustomCondition> {
		// TODO 需要测试能不能用
		public static final Codec<CustomCondition> CODEC =
				KeyDispatchCodec.unsafe(
						"id",
						Codec.STRING,
						(JsonElement it) -> {
							try {
								return DataResult.success(
										GsonHelper.getAsString(
												it.getAsJsonObject(),
												"id"
										));
							} catch (Exception e) {
								return DataResult.error(e::getMessage);
							}
						},
						(it) -> DataResult.success(ExtraCodecs.JSON),
						(it) -> DataResult.success(ExtraCodecs.JSON)
				).flatXmap(it -> {
					try {
						final var json = it.getAsJsonObject();
						return DataResult.success(new CustomCondition(
								GsonHelper.getAsString(json, "id"),
								json.getAsJsonObject()
						));
					} catch (Exception e) {
						return DataResult.error(e::getMessage);
					}
				}, it -> DataResult.success(it.data)).codec();

		@Override
		public Codec<CustomCondition> codec() {
			return CODEC;
		}
	}
}
