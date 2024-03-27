package snownee.lychee.util.recipe;


import java.util.Optional;

import net.minecraft.advancements.critereon.BlockPredicate;
import snownee.lychee.util.context.LycheeContext;

public interface BlockKeyableRecipe<T extends BlockKeyableRecipe<T>> extends Comparable<T>, ILycheeRecipe<LycheeContext> {
	Optional<BlockPredicate> blockPredicate();
}
