package snownee.lychee.action;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.Lychee;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.action.ActionRuntime.State;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.util.action.ActionMarker;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.recipe.LycheeRecipe;

public class Delay extends PostAction {

	public final float seconds;

	public Delay(float seconds) {
		this.seconds = seconds;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.DELAY;
	}

	@Override
	public void doApply(LycheeRecipe recipe, LycheeRecipeContext ctx, int times) {
		apply(recipe, ctx, times);
	}

	@Override
	protected void apply(LycheeRecipe recipe, LycheeRecipeContext ctx, int times) {
		if (ctx.runtime.marker == null) {
			makeMarker(recipe, ctx);
		}
		ctx.runtime.marker.lychee$addDelay((int) (seconds * 20));
		ctx.runtime.state = State.PAUSED;
	}

	public static void makeMarker(LycheeRecipe recipe, LycheeRecipeContext ctx) {
		Marker marker = EntityType.MARKER.create(ctx.getLevel());
		Vec3 pos = ctx.getParamOrNull(LootContextParams.ORIGIN);
		if (pos != null)
			marker.moveTo(pos);
		marker.setCustomName(Component.literal(Lychee.ID));
		ctx.getLevel().addFreshEntity(marker);
		ActionMarker actionMarker = (ActionMarker) marker;
		actionMarker.lychee$setContext(recipe, ctx);
		ctx.runtime.marker = actionMarker;
	}

	@Override
	public boolean preventSync() {
		return true;
	}

	public static class Type extends PostActionType<Delay> {

		@Override
		public Delay fromJson(JsonObject o) {
			return new Delay(o.get("s").getAsFloat());
		}

		@Override
		public void toJson(Delay action, JsonObject o) {
			o.addProperty("s", action.seconds);
		}

		@Override
		public Delay fromNetwork(FriendlyByteBuf buf) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void toNetwork(Delay action, FriendlyByteBuf buf) {
			throw new UnsupportedOperationException();
		}

	}

}
