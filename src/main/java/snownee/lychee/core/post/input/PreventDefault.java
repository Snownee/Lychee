package snownee.lychee.core.post.input;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import snownee.lychee.PostActionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.post.PostActionType;
import snownee.lychee.core.recipe.LycheeRecipe;

public class PreventDefault extends PostAction {

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.PREVENT_DEFAULT;
	}

	@Override
	public boolean doApply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		times = checkConditions(recipe, ctx, times);
		if (times > 0) {
			apply(recipe, ctx, times);
			return false;
		}
		return true;
	}

	@Override
	protected void apply(LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
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
		public PreventDefault fromNetwork(FriendlyByteBuf buf) {
			return new PreventDefault();
		}

		@Override
		public void toNetwork(PreventDefault action, FriendlyByteBuf buf) {
		}

	}

}
