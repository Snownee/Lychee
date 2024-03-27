package snownee.lychee.compat.rei.display;

import java.util.List;

import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultShapedDisplay;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.recipes.ShapedCraftingRecipe;

public class ShapedCraftingDisplay extends DefaultShapedDisplay implements LycheeDisplay<ShapedCraftingRecipe> {

	private final RecipeHolder<ShapedCraftingRecipe> recipe;

	public ShapedCraftingDisplay(RecipeHolder<ShapedCraftingRecipe> recipe) {
		super(new RecipeHolder<>(recipe.id(), recipe.value().shaped()));
		this.recipe = recipe;
	}

	@Override
	public ShapedCraftingRecipe recipe() {
		return recipe.value();
	}

	@Override
	public List<EntryIngredient> getOutputEntries() {
		var ingredients = LycheeDisplay.super.getOutputEntries();
		ingredients.addAll(0, super.getOutputEntries());
		return ingredients;
	}

}
