package snownee.lychee.util.recipe;


import java.util.Optional;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.world.Container;

public interface BlockKeyableRecipe<T extends BlockKeyableRecipe<T, C>, C extends Container> extends Comparable<T>, ILycheeRecipe<C> {
	Optional<BlockPredicate> blockPredicate();
}
