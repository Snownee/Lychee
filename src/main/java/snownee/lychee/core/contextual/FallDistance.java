package snownee.lychee.core.contextual;

import com.google.gson.JsonObject;

import net.minecraft.advancements.critereon.MinMaxBounds.Doubles;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.ContextualConditionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BoundsHelper;
import snownee.lychee.core.def.DoubleBoundsHelper;
import snownee.lychee.core.recipe.LycheeRecipe;

public record FallDistance(Doubles range) implements ContextualCondition {

	@Override
	public ContextualConditionType<? extends ContextualCondition> getType() {
		return ContextualConditionTypes.FALL_DISTANCE;
	}

	@Override
	public int test(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		Entity entity = ctx.getParam(LootContextParams.THIS_ENTITY);
		double d = entity.fallDistance;
		if (entity instanceof FallingBlockEntity block) {
			d = Math.max(block.getStartPos().getY() - block.getBlockY(), d);
		}
		return range.matches(d) ? times : 0;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		return Component.translatable(makeDescriptionId(inverted), BoundsHelper.getDescription(range));
	}

	public static class Type extends ContextualConditionType<FallDistance> {

		@Override
		public FallDistance fromJson(JsonObject o) {
			return new FallDistance(Doubles.fromJson(o.get("range")));
		}

		@Override
		public void toJson(FallDistance condition, JsonObject o) {
			o.add("range", condition.range().serializeToJson());
		}

		@Override
		public FallDistance fromNetwork(FriendlyByteBuf buf) {
			return new FallDistance(DoubleBoundsHelper.fromNetwork(buf));
		}

		@Override
		public void toNetwork(FallDistance condition, FriendlyByteBuf buf) {
			DoubleBoundsHelper.toNetwork(condition.range, buf);
		}

	}

}
