package snownee.lychee.compat.recipeviewer.category;

import snownee.lychee.compat.recipeviewer.RvHelper;

public abstract class RvCategoryBuilder {
	private final RvCategoryInstance<?> category;

	protected RvCategoryBuilder(RvCategoryInstance<?> category) {
		this.category = category;
	}

	public RvCategoryInstance<?> category() {
		return category;
	}

	public RvHelper helper() {
		return category().helper();
	}

	public int width() {
		return category().width();
	}

	public int height() {
		return category().height();
	}
}
