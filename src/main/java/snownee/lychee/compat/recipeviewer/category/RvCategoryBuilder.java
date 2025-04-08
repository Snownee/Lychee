package snownee.lychee.compat.recipeviewer.category;

import snownee.lychee.compat.recipeviewer.RvHelper;

public abstract class RvCategoryBuilder {
	private final RvCategoryInstance<?> instance;

	protected RvCategoryBuilder(RvCategoryInstance<?> instance) {
		this.instance = instance;
	}

	public RvCategoryInstance<?> instance() {
		return instance;
	}

	public RvCategory<?> type() {
		return instance().type();
	}

	public RvHelper helper() {
		return instance().helper();
	}

	public int width() {
		return instance().width();
	}

	public int height() {
		return instance().height();
	}
}
