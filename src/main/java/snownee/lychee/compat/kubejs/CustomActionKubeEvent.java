package snownee.lychee.compat.kubejs;

import com.google.gson.JsonObject;

import dev.latvian.mods.kubejs.event.KubeEvent;
import snownee.lychee.action.CustomAction;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class CustomActionKubeEvent implements KubeEvent {

	public final String id;
	public final CustomAction action;
	public final ILycheeRecipe<?> recipe;
	public final JsonObject data;

	public CustomActionKubeEvent(String id, CustomAction action, ILycheeRecipe<?> recipe) {
		this.id = id;
		this.action = action;
		this.recipe = recipe;
		this.data = action.data();
	}

	public void setApplyFunc(CustomAction.Apply applyFunc) {
		action.applyFunc = applyFunc;
	}

}
