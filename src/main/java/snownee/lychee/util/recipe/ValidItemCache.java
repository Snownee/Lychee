package snownee.lychee.util.recipe;

import java.util.List;

import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ValidItemCache {
	private IntSet validItems = IntSets.emptySet();

	public void refreshCache(List<? extends RecipeHolder<?>> recipes) {
		validItems = new IntAVLTreeSet(recipes.stream()
				.flatMap($ -> $.value().getIngredients().stream())
				.flatMapToInt($ -> $.getStackingIds().intStream())
				.toArray());
	}

	public boolean contains(ItemStack stack) {
		return validItems.contains(StackedContents.getStackingIndex(stack));
	}
}
