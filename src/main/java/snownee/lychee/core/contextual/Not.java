package snownee.lychee.core.contextual;

import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import snownee.lychee.ContextualConditionTypes;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.LUtil;

public record Not(ContextualCondition condition) implements ContextualCondition {

	@Override
	public ContextualConditionType<?> getType() {
		return ContextualConditionTypes.NOT;
	}

	@Override
	public int test(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		return times - condition.test(recipe, ctx, times);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public InteractionResult testInTooltips() {
		switch (condition.testInTooltips()) {
		case SUCCESS:
			return InteractionResult.FAIL;
		case FAIL:
			return InteractionResult.SUCCESS;
		default:
			return InteractionResult.PASS;
		}
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		return condition.getDescription(!inverted);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendTooltips(List<Component> tooltips, int indent, boolean inverted) {
		ContextualCondition.super.appendTooltips(tooltips, indent, !inverted);
	}

	public static class Type extends ContextualConditionType<Not> {

		@Override
		public Not fromJson(JsonObject o) {
			o = o.getAsJsonObject("contextual");
			ResourceLocation key = new ResourceLocation(o.get("type").getAsString());
			ContextualConditionType<?> type = LycheeRegistries.CONTEXTUAL.getValue(key);
			return new Not(type.fromJson(o));
		}

		@Override
		public Not fromNetwork(FriendlyByteBuf buf) {
			ContextualConditionType<?> type = LUtil.readRegistryId(LycheeRegistries.CONTEXTUAL, buf);
			return new Not(type.fromNetwork(buf));
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void toNetwork(Not condition, FriendlyByteBuf buf) {
			ContextualConditionType type = condition.condition.getType();
			LUtil.writeRegistryId(LycheeRegistries.CONTEXTUAL, type, buf);
			type.toNetwork(condition.condition, buf);
		}

	}

}
