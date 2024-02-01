package snownee.lychee.util.recipe;

import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

public class ValidItemCache {
	private IntSet validItems = IntSets.emptySet();

	public void refreshCache(List<? extends Recipe<?>> recipes) {
		validItems = new IntAVLTreeSet(recipes.stream()
											  .flatMap($ -> $.getIngredients().stream())
											  .flatMapToInt($ -> $.getStackingIds().intStream())
											  .toArray());
	}

	public boolean contains(ItemStack stack) {
		return validItems.contains(StackedContents.getStackingIndex(stack));
	}
}
