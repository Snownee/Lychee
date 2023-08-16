package snownee.lychee.core.contextual;

import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.ContextualConditionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.CommonProxy;

public class CustomCondition implements ContextualCondition {

	public final JsonObject data;
	public Test testFunc;
	public BiFunction<Level, @Nullable Player, InteractionResult> testInTooltipsFunc = (level, player) -> InteractionResult.PASS;

	public CustomCondition(JsonObject data) {
		this.data = data;
		CommonProxy.postCustomConditionEvent(GsonHelper.getAsString(data, "id"), this);
	}

	@Override
	public ContextualConditionType<?> getType() {
		return ContextualConditionTypes.CUSTOM;
	}

	@Override
	public int test(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		if (testFunc != null) {
			return testFunc.test(recipe, ctx, times);
		}
		return 0;
	}

	@Override
	public InteractionResult testInTooltips(Level level, @Nullable Player player) {
		return testInTooltipsFunc.apply(level, player);
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		return Component.translatable(makeDescriptionId(inverted), GsonHelper.getAsString(data, "id"));
	}

	@FunctionalInterface
	public interface Test {
		int test(ILycheeRecipe<?> recipe, LycheeContext ctx, int times);
	}

	public static class Type extends ContextualConditionType<CustomCondition> {

		@Override
		public CustomCondition fromJson(JsonObject o) {
			return new CustomCondition(o);
		}

		@Override
		public void toJson(CustomCondition condition, JsonObject o) {
			condition.data.entrySet().forEach(e -> o.add(e.getKey(), e.getValue()));
		}

		@Override
		public CustomCondition fromNetwork(FriendlyByteBuf buf) {
			return new CustomCondition(JsonParser.parseString(buf.readUtf()).getAsJsonObject());
		}

		@Override
		public void toNetwork(CustomCondition condition, FriendlyByteBuf buf) {
			buf.writeUtf(condition.data.toString());
		}

	}

}
