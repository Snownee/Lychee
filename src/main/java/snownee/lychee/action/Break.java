package snownee.lychee.action;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.action.ActionRuntime.State;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.core.recipe.recipe.LycheeRecipe;

public class Break extends PostAction {

	public static final Break CLIENT_DUMMY = new Break();

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.BREAK;
	}

	@Override
	public void doApply(LycheeRecipe<?> recipe, LycheeRecipeContext ctx, int times) {
		ctx.runtime.state = State.STOPPED;
	}

	@Override
	protected void apply(LycheeRecipe<?> recipe, LycheeRecipeContext ctx, int times) {
	}

	@Override
	public boolean hidden() {
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
