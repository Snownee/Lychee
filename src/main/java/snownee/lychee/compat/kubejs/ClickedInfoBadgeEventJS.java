package snownee.lychee.compat.kubejs;

import dev.latvian.mods.kubejs.client.ClientEventJS;
import snownee.lychee.core.recipe.ILycheeRecipe;

public class ClickedInfoBadgeEventJS extends ClientEventJS {

	public final ILycheeRecipe<?> recipe;
	public final int button;

	public ClickedInfoBadgeEventJS(ILycheeRecipe<?> recipe, int button) {
		this.recipe = recipe;
		this.button = button;
	}

}
