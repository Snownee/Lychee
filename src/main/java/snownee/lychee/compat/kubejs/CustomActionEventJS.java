package snownee.lychee.compat.kubejs;

import java.util.Map;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.util.MapJS;
import snownee.lychee.core.post.CustomAction;
import snownee.lychee.core.recipe.ILycheeRecipe;

public class CustomActionEventJS extends EventJS {

	public final String id;
	public final CustomAction action;
	public final ILycheeRecipe<?> recipe;
	public final ILycheeRecipe.NBTPatchContext patchContext;
	public final Map<?, ?> data;

	public CustomActionEventJS(String id, CustomAction action, ILycheeRecipe<?> recipe, ILycheeRecipe.NBTPatchContext patchContext) {
		this.id = id;
		this.action = action;
		this.recipe = recipe;
		this.patchContext = patchContext;
		this.data = MapJS.of(action.data);
	}

}
