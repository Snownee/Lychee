package snownee.lychee.compat.recipeviewer.category;

import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public abstract class RvCategoryWidgetBuilder<R extends ILycheeRecipe<LycheeContext>> extends RvCategoryBuilder<R> {
	protected RvCategoryWidgetBuilder(RvCategoryInstance<R> instance, RecipeHolder<R> recipeHolder) {
		super(instance, recipeHolder);
	}

	public abstract void addElement(RenderElement element);
}
