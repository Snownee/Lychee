package snownee.lychee.util.action;

import net.minecraft.world.entity.Marker;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.util.recipe.OldLycheeRecipe;

public interface ActionMarker {
	void lychee$setContext(RecipeHolder<OldLycheeRecipe<?>> recipe, LycheeRecipeContext ctx);

	void lychee$addDelay(int delay);

	LycheeRecipeContext lychee$getContext();

	default Marker self() {
		return (Marker) this;
	}
}
