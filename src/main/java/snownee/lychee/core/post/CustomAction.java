package snownee.lychee.core.post;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import snownee.lychee.PostActionTypes;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.util.LUtil;

public class CustomAction extends PostAction {

	public final JsonObject data;
	public boolean canRepeat;
	public Apply applyFunc;

	public CustomAction(JsonObject data) {
		this.data = data;
	}

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.CUSTOM;
	}

	@Override
	public void doApply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		apply(recipe, ctx, times);
	}

	@Override
	protected void apply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		if (applyFunc != null) {
			applyFunc.apply(recipe, ctx, times);
		}
	}

	@Override
	public boolean preventSync() {
		return true;
	}

	@Override
	public boolean canRepeat() {
		return canRepeat;
	}

	@Override
	public void validate(ILycheeRecipe<?> recipe, ILycheeRecipe.NBTPatchContext patchContext) {
		LUtil.postCustomActionEvent(GsonHelper.getAsString(data, "id"), this, recipe, patchContext);
	}

	@FunctionalInterface
	public interface Apply {
		void apply(ILycheeRecipe<?> recipe, LycheeContext ctx, int times);
	}

	public static class Type extends PostActionType<CustomAction> {

		@Override
		public CustomAction fromJson(JsonObject o) {
			return new CustomAction(o);
		}

		@Override
		public void toJson(CustomAction action, JsonObject o) {
			action.data.entrySet().forEach(e -> o.add(e.getKey(), e.getValue()));
		}

		@Override
		public CustomAction fromNetwork(FriendlyByteBuf buf) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void toNetwork(CustomAction action, FriendlyByteBuf buf) {
			throw new UnsupportedOperationException();
		}

	}
}
