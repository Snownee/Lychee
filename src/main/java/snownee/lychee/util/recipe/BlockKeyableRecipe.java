package snownee.lychee.util.recipe;


import java.util.Optional;

import net.minecraft.advancements.critereon.BlockPredicate;

public interface BlockKeyableRecipe<T extends BlockKeyableRecipe<T>> extends Comparable<T>, LycheeRecipe<T> {
	Optional<BlockPredicate> blockPredicate();
}
