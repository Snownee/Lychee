package snownee.lychee.ui;

import java.util.List;

import snownee.lychee.util.category.UIElement;
import snownee.lychee.util.category.UIElementType;

public record CompoundElement(List<Child> children) implements UIElement {
	@Override
	public UIElementType<?> type() {
		return null;
	}

	public record Child(UIElement element) {

	}
}
