package snownee.lychee.compat.rei.display;

import java.util.List;

import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultShapedDisplay;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.crafting.ShapedCraftingRecipe;

public class ShapedCraftingDisplay extends DefaultShapedDisplay implements DisplayRecipeProvider {

	private final ShapedCraftingRecipe recipe;

	public ShapedCraftingDisplay(ShapedCraftingRecipe recipe) {
		super(recipe);
		this.recipe = recipe;
	}

	@Override
	public ILycheeRecipe<?> recipe() {
		return recipe;
	}

	@Override
	public List<EntryIngredient> getOutputEntries() {
		List<EntryIngredient> ingredients = BaseREIDisplay.getOutputEntries(recipe());
		ingredients.addAll(0, super.getOutputEntries());
		return ingredients;
	}

}
