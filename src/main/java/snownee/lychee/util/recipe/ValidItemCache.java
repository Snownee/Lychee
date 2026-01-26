package snownee.lychee.util.recipe;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ValidItemCache {
	private ReferenceSet<Holder<Item>> validItems = ReferenceSet.of();

	public void refreshCache(List<? extends RecipeHolder<? extends ILycheeRecipe<?>>> recipes) {
		validItems = new ReferenceOpenHashSet<>(recipes.stream()
				.flatMap($ -> $.value().getIngredients().stream())
				.flatMap(Ingredient::items)
				.toList());
	}

	public boolean contains(ItemStack stack) {
		return validItems.contains(stack.typeHolder());
	}
}
