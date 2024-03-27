package snownee.lychee.compat.rei.display;

import java.util.List;
import java.util.stream.Stream;

import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.plugin.common.displays.anvil.AnvilRecipe;
import me.shedaniel.rei.plugin.common.displays.anvil.DefaultAnvilDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.recipes.AnvilCraftingRecipe;

public class AnvilCraftingDisplay extends DefaultAnvilDisplay implements LycheeDisplay<AnvilCraftingRecipe> {

	private final RecipeHolder<AnvilCraftingRecipe> lycheeRecipe;

	public AnvilCraftingDisplay(RecipeHolder<AnvilCraftingRecipe> recipeHolder) {
		super(makeRecipe(recipeHolder));
		this.lycheeRecipe = recipeHolder;
	}

	private static AnvilRecipe makeRecipe(RecipeHolder<AnvilCraftingRecipe> recipe) {
		var right = Stream.of(recipe.value().input().getSecond().getItems())
				.map(ItemStack::copy)
				.peek(it -> it.setCount(recipe.value().materialCost()))
				.toList();
		return new AnvilRecipe(
				recipe.id(),
				List.of(recipe.value().input().getFirst().getItems()),
				right,
				List.of(recipe.value().getResultItem(Minecraft.getInstance().level.registryAccess())));
	}

	@Override
	public AnvilCraftingRecipe recipe() {
		return lycheeRecipe.value();
	}

	@Override
	public List<EntryIngredient> getOutputEntries() {
		var ingredients = LycheeDisplay.super.getOutputEntries();
		ingredients.addAll(0, super.getOutputEntries());
		return ingredients;
	}
}
