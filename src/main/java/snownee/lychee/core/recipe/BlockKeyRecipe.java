package snownee.lychee.core.recipe;

import net.minecraft.advancements.critereon.BlockPredicate;

public interface BlockKeyRecipe<T> extends Comparable<T> {

	BlockPredicate getBlock();

}