package snownee.lychee.util.recipe;


import net.minecraft.advancements.critereon.BlockPredicate;
import snownee.lychee.util.context.LycheeContext;

public interface BlockKeyableRecipe extends ILycheeRecipe<LycheeContext> {
	BlockPredicate blockPredicate();
}
