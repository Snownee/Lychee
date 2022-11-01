package snownee.lychee.core.post;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import snownee.lychee.PostActionTypes;
import snownee.lychee.core.ActionRuntime.State;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;

public class Break extends PostAction {

	public static final Break CLIENT_DUMMY = new Break();

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.BREAK;
	}

	@Override
	public void doApply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		ctx.runtime.state = State.STOPPED;
	}

	@Override
	protected void apply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
	}

	@Override
	public boolean isHidden() {
		return true;
	}

	public static class Type extends PostActionType<Break> {

		@Override
		public Break fromJson(JsonObject o) {
			return new Break();
		}

		@Override
		public void toJson(Break action, JsonObject o) {
		}

		@Override
		public Break fromNetwork(FriendlyByteBuf buf) {
			return CLIENT_DUMMY;
		}

		@Override
		public void toNetwork(Break action, FriendlyByteBuf buf) {
		}

	}

}
