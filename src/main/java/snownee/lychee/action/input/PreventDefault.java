package snownee.lychee.action.input;

import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.compat.IngredientInfo;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.recipe.OldLycheeRecipe;

public class PreventDefault extends PostAction {

	public static final PreventDefault CLIENT_DUMMY = new PreventDefault();

	@Override
	public PostActionType<?> getType() {
		return PostActionTypes.PREVENT_DEFAULT;
	}

	@Override
	public void doApply(LycheeRecipe<?> recipe, LycheeRecipeContext ctx, int times) {
		ctx.runtime.doDefault = false;
	}

	@Override
	protected void apply(LycheeRecipe<?> recipe, LycheeRecipeContext ctx, int times) {
	}

	@Override
	public boolean hidden() {
		return true;
	}

	@Override
	public void loadCatalystsInfo(LycheeRecipe<?> recipe, List<IngredientInfo> ingredients) {
		if (recipe instanceof OldLycheeRecipe<?> lycheeRecipe && lycheeRecipe.getType().canPreventConsumeInputs) {
			for (var ingredient : ingredients) {
				if (ingredient.tooltips.isEmpty()) {
					ingredient.addTooltip(lycheeRecipe.getType().getPreventDefaultDescription(lycheeRecipe));
					ingredient.isCatalyst = true;
				}
			}
		}
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
