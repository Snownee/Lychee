package snownee.lychee.ui;

import java.util.List;

import snownee.lychee.util.ui.UIElement;
import snownee.lychee.util.ui.UIElementCommonProperties;
import snownee.lychee.util.ui.UIElementType;

public record CompoundElement(UIElementCommonProperties commonProperties, List<Child> children) implements UIElement {
	@Override
	public UIElementType<?> type() {
		return null;
	}

	public record Child(UIElement element) {

	}
}
