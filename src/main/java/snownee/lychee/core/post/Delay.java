package snownee.lychee.core.post;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.Lychee;
import snownee.lychee.PostActionTypes;
import snownee.lychee.core.ActionRuntime.State;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ILycheeRecipe;

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
	public void doApply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		apply(recipe, ctx, times);
	}

	@Override
	protected void apply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		if (ctx.runtime.marker == null) {
			makeMarker(recipe, ctx);
		}
		ctx.runtime.marker.lychee$addDelay((int) (seconds * 20));
		ctx.runtime.state = State.PAUSED;
	}

	public static void makeMarker(ILycheeRecipe<?> recipe, LycheeContext ctx) {
		Marker marker = EntityType.MARKER.create(ctx.getLevel());
		Vec3 pos = ctx.getParamOrNull(LootContextParams.ORIGIN);
		if (pos != null)
			marker.moveTo(pos);
		marker.setCustomName(Component.literal(Lychee.ID));
		ctx.getLevel().addFreshEntity(marker);
		LycheeMarker lycheeMarker = (LycheeMarker) marker;
		lycheeMarker.lychee$setContext(recipe, ctx);
		ctx.runtime.marker = lycheeMarker;
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

	public interface LycheeMarker {
		void lychee$setContext(ILycheeRecipe<?> recipe, LycheeContext ctx);

		void lychee$addDelay(int delay);

		LycheeContext lychee$getContext();

		default Marker getEntity() {
			return (Marker) this;
		}
	}

}
