package snownee.lychee.core.contextual;

import java.util.List;

import com.google.gson.JsonObject;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionResult;
import snownee.lychee.ContextualConditionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;

public class Or extends ContextualHolder implements ContextualCondition {

	@Override
	public ContextualConditionType<?> getType() {
		return ContextualConditionTypes.OR;
	}

	@Override
	public int test(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		for (ContextualCondition condition : getConditions()) {
			int result = condition.test(recipe, ctx, times);
			if (result > 0) {
				return result;
			}
		}
		return 0;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public InteractionResult testInTooltips() {
		boolean allFailed = true;
		for (ContextualCondition condition : getConditions()) {
			InteractionResult result = condition.testInTooltips();
			if (result == InteractionResult.SUCCESS) {
				return result;
			}
			if (result != InteractionResult.FAIL) {
				allFailed = false;
			}
		}
		return allFailed ? InteractionResult.FAIL : InteractionResult.PASS;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		return new TranslatableComponent(makeDescriptionId(inverted));
	}

	@Override
	public void appendTooltips(List<Component> tooltips, int indent, boolean inverted) {
		ContextualCondition.super.appendTooltips(tooltips, indent, inverted);
		for (ContextualCondition condition : getConditions()) {
			condition.appendTooltips(tooltips, indent + 1, false);
		}
	}

	public static class Type extends ContextualConditionType<Or> {

		@Override
		public Or fromJson(JsonObject o) {
			Or or = new Or();
			or.parseConditions(o.get("contextual"));
			return or;
		}

		@Override
		public Or fromNetwork(FriendlyByteBuf buf) {
			Or or = new Or();
			or.conditionsFromNetwork(buf);
			return or;
		}

		@Override
		public void toNetwork(Or condition, FriendlyByteBuf buf) {
			condition.conditionsToNetwork(buf);
		}

	}

}
