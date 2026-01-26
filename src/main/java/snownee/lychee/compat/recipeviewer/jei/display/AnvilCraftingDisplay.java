package snownee.lychee.compat.recipeviewer.jei.display;

import java.util.List;

import org.jetbrains.annotations.Unmodifiable;

import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.recipes.AnvilCraftingRecipe;


public record AnvilCraftingDisplay(
		RecipeHolder<AnvilCraftingRecipe> recipeHolder,
		List<ItemStack> left,
		List<ItemStack> right) implements IJeiAnvilRecipe {

	public static AnvilCraftingDisplay of(RecipeHolder<AnvilCraftingRecipe> recipeHolder, ContextMap context) {
		AnvilCraftingRecipe recipe = recipeHolder.value();
		List<Ingredient> ingredients = recipe.getIngredients();
		List<ItemStack> left = ingredients.getFirst().display().resolveForStacks(context);
		List<ItemStack> right = ingredients.size() == 1 ? List.of() : ingredients.getLast().display().resolveForStacks(context)
				.stream()
				.peek(it -> it.setCount(recipe.materialCost()))
				.toList();
		return new AnvilCraftingDisplay(recipeHolder, left, right);
	}

	@Override
	public @Unmodifiable List<ItemStack> getLeftInputs() {
		return left;
	}

	@Override
	public @Unmodifiable List<ItemStack> getRightInputs() {
		return right;
	}

	@Override
	public @Unmodifiable List<ItemStack> getOutputs() {
		return List.of(recipeHolder.value().output().create());
	}

	@Override
	public Identifier getUid() {
		return recipeHolder.id().identifier();
	}
}
