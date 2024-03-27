package snownee.lychee.core.post.input;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import snownee.lychee.PostActionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.core.recipe.ILycheeRecipe;

public class PreventDefault extends PostAction {

	public static final PreventDefault CLIENT_DUMMY = new PreventDefault();

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.PREVENT_DEFAULT;
	}

	@Override
	public void doApply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		ctx.runtime.doDefault = false;
	}

	@Override
	protected void apply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
	}

	@Override
	public boolean isHidden() {
		return true;
	}

	public static class Type extends PostActionType<PreventDefault> {

		@Override
		public PreventDefault fromJson(JsonObject o) {
			return new PreventDefault();
		}

		@Override
		public void toJson(PreventDefault action, JsonObject o) {
		}

		@Override
		public PreventDefault fromNetwork(FriendlyByteBuf buf) {
			return CLIENT_DUMMY;
		}

		@Override
		public void toNetwork(PreventDefault action, FriendlyByteBuf buf) {
		}

	}

}
