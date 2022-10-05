package snownee.lychee.core.post;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.PostActionTypes;
import snownee.lychee.core.ActionRuntime.State;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;

public class Delay extends PostAction {

	public static final Delay CLIENT_DUMMY = new Delay(0);
	public final float seconds;

	public Delay(float seconds) {
		this.seconds = seconds;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.DELAY;
	}

	@Override
	public void doApply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		apply(recipe, ctx, times);
	}

	@Override
	protected void apply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		if (ctx.runtime.marker == null) {
			makeMarker(recipe, ctx);
		}
		ctx.runtime.marker.lychee$addDelay((int) (seconds * 20));
		ctx.runtime.state = State.PAUSED;
	}

	public static void makeMarker(LycheeRecipe<?> recipe, LycheeContext ctx) {
		Vec3 pos = ctx.getParam(LootContextParams.ORIGIN);
		Marker marker = EntityType.MARKER.create(ctx.getLevel());
		marker.moveTo(pos);
		ctx.getLevel().addFreshEntity(marker);
		LycheeMarker lycheeMarker = (LycheeMarker) marker;
		lycheeMarker.lychee$setContext(recipe, ctx);
		ctx.runtime.marker = lycheeMarker;
	}

	@Override
	public boolean isHidden() {
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
			return CLIENT_DUMMY;
		}

		@Override
		public void toNetwork(Delay action, FriendlyByteBuf buf) {
		}

	}

	public interface LycheeMarker {
		void lychee$setContext(LycheeRecipe<?> recipe, LycheeContext ctx);

		void lychee$addDelay(int delay);

		LycheeContext lychee$getContext();

		default Marker getEntity() {
			return (Marker) this;
		}
	}

}
