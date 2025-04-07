package snownee.lychee.compat.recipeviewer.category;

import snownee.lychee.client.gui.RenderElement;

public abstract class RvCategoryWidgetBuilder extends RvCategoryBuilder {
	protected RvCategoryWidgetBuilder(RvCategoryInstance<?> category) {
		super(category);
	}

	public abstract void addElement(RenderElement element);
}
