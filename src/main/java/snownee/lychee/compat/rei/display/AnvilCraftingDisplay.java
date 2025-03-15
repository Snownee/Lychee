package snownee.lychee.compat.rei.display;

import java.util.List;
import java.util.stream.Stream;

import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.plugin.common.displays.anvil.AnvilRecipe;
import me.shedaniel.rei.plugin.common.displays.anvil.DefaultAnvilDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.recipes.AnvilCraftingRecipe;

public class AnvilCraftingDisplay extends DefaultAnvilDisplay implements LycheeDisplay<AnvilCraftingRecipe> {

	private final RecipeHolder<AnvilCraftingRecipe> lycheeRecipe;

	public AnvilCraftingDisplay(RecipeHolder<AnvilCraftingRecipe> recipeHolder) {
		super(makeRecipe(recipeHolder));
		this.lycheeRecipe = recipeHolder;
	}

	private static AnvilRecipe makeRecipe(RecipeHolder<AnvilCraftingRecipe> recipeHolder) {
		var recipe = recipeHolder.value();
		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		List<ItemStack> right = ingredients.size() == 1 ? List.of() : Stream.of(ingredients.getLast().getItems())
				.map(ItemStack::copy)
				.peek(it -> it.setCount(recipe.materialCost()))
				.toList();
		return new AnvilRecipe(
				recipeHolder.id(),
				List.of(ingredients.getFirst().getItems()),
				right,
				List.of(recipe.getResultItem(Minecraft.getInstance().level.registryAccess())));
	}

	@Override
	public RecipeHolder<AnvilCraftingRecipe> recipe() {
		return lycheeRecipe;
	}

	@Override
	public List<EntryIngredient> getOutputEntries() {
		var ingredients = LycheeDisplay.super.getOutputEntries();
		ingredients.addAll(0, super.getOutputEntries());
		return ingredients;
	}
}
