package snownee.lychee.compat.kubejs;

import dev.latvian.mods.kubejs.client.ClientEventJS;
import snownee.lychee.core.recipe.ILycheeRecipe;

public class ClickedInfoBadgeEventJS extends ClientEventJS {

	private final ILycheeRecipe<?> recipe;
	private final int button;

	public ClickedInfoBadgeEventJS(ILycheeRecipe<?> recipe, int button) {
		this.recipe = recipe;
		this.button = button;
	}

	public ILycheeRecipe<?> getRecipe() {
		return recipe;
	}

	public int getButton() {
		return button;
	}
}
