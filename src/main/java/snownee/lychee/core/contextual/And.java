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

public class And extends ContextualHolder implements ContextualCondition {

	@Override
	public ContextualConditionType<?> getType() {
		return ContextualConditionTypes.AND;
	}

	@Override
	public int test(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		return checkConditions(recipe, ctx, times);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public InteractionResult testInTooltips() {
		boolean allSuccess = true;
		for (ContextualCondition condition : getConditions()) {
			InteractionResult result = condition.testInTooltips();
			if (result == InteractionResult.FAIL) {
				return result;
			}
			if (result != InteractionResult.SUCCESS) {
				allSuccess = false;
			}
		}
		return allSuccess ? InteractionResult.SUCCESS : InteractionResult.PASS;
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

	@Override
	public int showingCount() {
		return showingConditionsCount();
	}

	public static class Type extends ContextualConditionType<And> {

		@Override
		public And fromJson(JsonObject o) {
			And and = new And();
			and.parseConditions(o.get("contextual"));
			return and;
		}

		@Override
		public And fromNetwork(FriendlyByteBuf buf) {
			And and = new And();
			and.conditionsFromNetwork(buf);
			return and;
		}

		@Override
		public void toNetwork(And condition, FriendlyByteBuf buf) {
			condition.conditionsToNetwork(buf);
		}

	}

}
