package snownee.lychee.compat.jei.display;

import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.Unmodifiable;

import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.recipes.AnvilCraftingRecipe;

@NotNullByDefault
public record AnvilCraftingDisplay(
		RecipeHolder<AnvilCraftingRecipe> recipeHolder,
		List<ItemStack> left,
		List<ItemStack> right) implements IJeiAnvilRecipe {

	public static AnvilCraftingDisplay of(RecipeHolder<AnvilCraftingRecipe> recipeHolder) {
		AnvilCraftingRecipe recipe = recipeHolder.value();
		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		List<ItemStack> left = List.of(ingredients.getFirst().getItems());
		List<ItemStack> right = ingredients.size() == 1 ? List.of() : Stream.of(ingredients.getLast().getItems())
				.map(ItemStack::copy)
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
		return List.of(recipeHolder.value().output());
	}

	@Override
	public ResourceLocation getUid() {
		return recipeHolder.id();
	}
}
