package snownee.lychee.compat.recipeviewer.rei.display;

import java.util.List;
import java.util.Objects;

import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.displays.anvil.AnvilRecipe;
import me.shedaniel.rei.plugin.common.displays.anvil.DefaultAnvilDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import snownee.lychee.recipes.AnvilCraftingRecipe;

public class AnvilCraftingDisplay extends DefaultAnvilDisplay implements LycheeDisplay<AnvilCraftingRecipe> {
	private final RecipeHolder<AnvilCraftingRecipe> lycheeRecipe;

	public AnvilCraftingDisplay(RecipeHolder<AnvilCraftingRecipe> recipeHolder) {
		super(makeRecipe(recipeHolder));
		this.lycheeRecipe = recipeHolder;
	}

	private static AnvilRecipe makeRecipe(RecipeHolder<AnvilCraftingRecipe> recipeHolder) {
		var recipe = recipeHolder.value();
		List<Ingredient> ingredients = recipe.getIngredients();
		ContextMap context = SlotDisplayContext.fromLevel(Objects.requireNonNull(Minecraft.getInstance().level));
		List<ItemStack> left = ingredients.getFirst().display().resolveForStacks(context);
		List<ItemStack> right = ingredients.size() == 1 ? List.of() : ingredients.getLast().display().resolveForStacks(context)
				.stream()
				.peek(it -> it.setCount(recipe.materialCost()))
				.toList();
		return new AnvilRecipe(recipeHolder.id().identifier(), left, right, List.of(recipe.output().create()));
	}

	@Override
	public RecipeHolder<AnvilCraftingRecipe> recipeHolder() {
		return lycheeRecipe;
	}

	@Override
	public List<EntryIngredient> getOutputEntries() {
		var ingredients = LycheeDisplay.super.getOutputEntries();
		ingredients.addAll(0, super.getOutputEntries());
		return ingredients;
	}

	@Override
	public java.util.Optional<Identifier> getDisplayLocation() {
		return java.util.Optional.of(lycheeRecipe.id().identifier());
	}
}
