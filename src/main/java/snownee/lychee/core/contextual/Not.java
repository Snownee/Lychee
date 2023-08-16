package snownee.lychee.core.contextual;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import snownee.lychee.ContextualConditionTypes;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.CommonProxy;

public record Not(ContextualCondition condition) implements ContextualCondition {

	@Override
	public ContextualConditionType<?> getType() {
		return ContextualConditionTypes.NOT;
	}

	@Override
	public int test(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		return times - condition.test(recipe, ctx, times);
	}

	@Override
	public InteractionResult testInTooltips(Level level, @Nullable Player player) {
		return switch (condition.testInTooltips(level, player)) {
			case SUCCESS -> InteractionResult.FAIL;
			case FAIL -> InteractionResult.SUCCESS;
			default -> InteractionResult.PASS;
		};
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		return condition.getDescription(!inverted);
	}

	@Override
	public void appendTooltips(List<Component> tooltips, Level level, @Nullable Player player, int indent, boolean inverted) {
		ContextualCondition.super.appendTooltips(tooltips, level, player, indent, !inverted);
	}

	public static class Type extends ContextualConditionType<Not> {

		@Override
		public Not fromJson(JsonObject o) {
			o = o.getAsJsonObject("contextual");
			ResourceLocation key = new ResourceLocation(o.get("type").getAsString());
			ContextualConditionType<?> type = LycheeRegistries.CONTEXTUAL.get(key);
			return new Not(type.fromJson(o));
		}

		@Override
		public void toJson(Not condition, JsonObject o) {
			o.add("contextual", condition.condition().toJson());
		}

		@Override
		public Not fromNetwork(FriendlyByteBuf buf) {
			ContextualConditionType<?> type = CommonProxy.readRegistryId(LycheeRegistries.CONTEXTUAL, buf);
			return new Not(type.fromNetwork(buf));
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void toNetwork(Not condition, FriendlyByteBuf buf) {
			ContextualConditionType type = condition.condition().getType();
			CommonProxy.writeRegistryId(LycheeRegistries.CONTEXTUAL, type, buf);
			type.toNetwork(condition.condition(), buf);
		}

	}

}
