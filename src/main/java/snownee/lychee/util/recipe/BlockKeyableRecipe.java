package snownee.lychee.util.recipe;


import net.minecraft.advancements.criterion.BlockPredicate;
import snownee.lychee.util.IngredientCollection;
import snownee.lychee.util.context.LycheeContext;

public interface BlockKeyableRecipe extends ILycheeRecipe<LycheeContext> {
	BlockPredicate blockPredicate();

	@Override
	default IngredientCollection ingredientCollection() {
		return IngredientCollection.EMPTY;
	}
}
