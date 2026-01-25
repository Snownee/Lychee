package snownee.lychee.client.gui;

import org.jspecify.annotations.Nullable;

public interface WrapperRenderElement {
	@Nullable ScreenElement getWrappedElement();

	static ScreenElement unwrap(ScreenElement element) {
		while (element instanceof WrapperRenderElement wrapper) {
			ScreenElement wrapped = wrapper.getWrappedElement();
			if (wrapped == null) {
				return element;
			}
			if (wrapped == element) {
				throw new IllegalArgumentException("Element " + element + " is wrapping itself!");
			}
			element = wrapped;
		}
		return element;
	}
}
