package snownee.lychee.core.post;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.PostActionTypes;
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
		makeDelayedActions(recipe.getId(), ctx);
	}

	public static void makeDelayedActions(ResourceLocation recipeId, LycheeContext ctx) {
		Vec3 pos = ctx.getParam(LootContextParams.ORIGIN);
		Marker marker = EntityType.MARKER.create(ctx.getLevel());
		marker.setPos(pos);
		LycheeMarker lycheeMarker = (LycheeMarker) marker;
		lycheeMarker.lychee$setContext(recipeId, ctx);
	}

	@Override
	public boolean isHidden() {
		return true;
	}

	public static class Type extends PostActionType<Delay> {

		@Override
		public Delay fromJson(JsonObject o) {
			return new Delay(o.get("seconds").getAsFloat());
		}

		@Override
		public void toJson(Delay action, JsonObject o) {
			o.addProperty("seconds", action.seconds);
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
		void lychee$setContext(ResourceLocation recipeId, LycheeContext ctx);

		LycheeContext lychee$getContext();
	}

}
