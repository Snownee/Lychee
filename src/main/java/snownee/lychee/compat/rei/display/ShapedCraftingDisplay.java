package snownee.lychee.compat.rei.display;

import me.shedaniel.rei.plugin.common.displays.crafting.DefaultShapedDisplay;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.crafting.ShapedCraftingRecipe;

public class ShapedCraftingDisplay extends DefaultShapedDisplay implements LycheeCraftingDisplay {

	private final ShapedCraftingRecipe recipe;

	public ShapedCraftingDisplay(ShapedCraftingRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override
	public ILycheeRecipe<?> recipe() {
		return recipe;
	}

}
