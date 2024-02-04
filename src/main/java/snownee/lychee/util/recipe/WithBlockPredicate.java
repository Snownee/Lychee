package snownee.lychee.util.recipe;

import net.minecraft.advancements.critereon.BlockPredicate;

public interface WithBlockPredicate<T extends WithBlockPredicate<T>> extends Comparable<T>, LycheeRecipe<T> {
	BlockPredicate blockPredicate();
}
