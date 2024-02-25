package snownee.lychee.util.recipe;


import java.util.Optional;

import net.minecraft.advancements.critereon.BlockPredicate;
import snownee.lychee.util.context.LycheeContext;

public interface BlockKeyableRecipe<T extends BlockKeyableRecipe<T, C>, C extends LycheeContext> extends Comparable<T>, ILycheeRecipe<C> {
	Optional<BlockPredicate> blockPredicate();
}
