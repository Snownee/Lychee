package snownee.lychee.compat.rei.display;

import java.util.List;

import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.plugin.common.displays.anvil.AnvilRecipe;
import me.shedaniel.rei.plugin.common.displays.anvil.DefaultAnvilDisplay;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.anvil_crafting.AnvilCraftingRecipe;
import snownee.lychee.core.recipe.ILycheeRecipe;

public class AnvilCraftingDisplay extends DefaultAnvilDisplay implements DisplayRecipeProvider {

	private final AnvilCraftingRecipe lycheeRecipe;

	public AnvilCraftingDisplay(AnvilCraftingRecipe lycheeRecipe) {
		super(makeRecipe(lycheeRecipe));
		this.lycheeRecipe = lycheeRecipe;
	}

	private static AnvilRecipe makeRecipe(AnvilCraftingRecipe $) {
		List<ItemStack> right = List.of($.getRight().getItems()).stream().map(ItemStack::copy).peek($$ -> $$.setCount($.getMaterialCost())).toList();
		return new AnvilRecipe($.getId(), List.of($.getLeft().getItems()), right, List.of($.getResultItem()));
	}

	@Override
	public ILycheeRecipe<?> recipe() {
		return lycheeRecipe;
	}

	@Override
	public List<EntryIngredient> getOutputEntries() {
		List<EntryIngredient> ingredients = BaseREIDisplay.getOutputEntries(recipe());
		ingredients.addAll(0, super.getOutputEntries());
		return ingredients;
	}
}
