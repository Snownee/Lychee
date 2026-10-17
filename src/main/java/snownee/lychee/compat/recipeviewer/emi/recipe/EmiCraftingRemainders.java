package snownee.lychee.compat.recipeviewer.emi.recipe;

import java.util.List;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.action.input.DamageItem;
import snownee.lychee.action.input.SetItem;
import snownee.lychee.recipes.ShapedCraftingRecipe;
import snownee.lychee.util.action.PostAction;

/**
 * Computes the remainders shown by EMI for {@link ShapedCraftingRecipe}.
 * <p>
 * EMI queries {@code getRemainingItems} with a synthetic crafting input which never went through
 * {@code matches}, so there is no context available to apply post actions with. Instead of simulating
 * a craft in the recipe viewer, derive the remainders from the recipe definition itself.
 */
public final class EmiCraftingRemainders {
	private EmiCraftingRemainders() {
	}

	public static void setRemainders(List<EmiIngredient> input, ShapedCraftingRecipe recipe) {
		var width = recipe.getWidth();
		var ingredients = recipe.getIngredients();
		for (var i = 0; i < input.size(); i++) {
			var column = i % 3;
			if (column >= width) {
				continue; // padding added by EMI to fill rows of 3
			}
			var index = i / 3 * width + column;
			if (index >= ingredients.size()) {
				continue;
			}
			for (var stack : input.get(i).getEmiStacks()) {
				var itemStack = stack.getItemStack();
				if (itemStack.isEmpty()) {
					continue;
				}
				var remainder = getRemainder(recipe, index, itemStack);
				if (!remainder.isEmpty()) {
					stack.setRemainder(EmiStack.of(remainder));
				}
			}
		}
	}

	private static ItemStack getRemainder(ShapedCraftingRecipe recipe, int index, ItemStack itemStack) {
		var item = itemStack.getItem();
		var remainder = item.hasCraftingRemainingItem() ? new ItemStack(item.getCraftingRemainingItem()) : ItemStack.EMPTY;
		for (var action : recipe.postActions()) {
			IntList indexes = switch (action) {
				case DamageItem damageItem -> recipe.getItemIndexes(damageItem.target());
				case SetItem setItem -> recipe.getItemIndexes(setItem.target());
				default -> null;
			};
			if (indexes == null || !indexes.contains(index)) {
				continue;
			}
			var transformed = action.transformRemainder(itemStack, recipe);
			if (!transformed.isEmpty()) {
				remainder = transformed;
			}
		}
		return remainder;
	}
}
