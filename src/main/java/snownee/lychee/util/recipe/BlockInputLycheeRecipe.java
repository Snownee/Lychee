package snownee.lychee.util.recipe;


import java.util.Optional;

import snownee.lychee.util.predicates.BlockPredicate;

public interface BlockInputLycheeRecipe<T extends BlockInputLycheeRecipe<T>> extends Comparable<T>, LycheeRecipe<T> {
	Optional<BlockPredicate> blockPredicate();
}
