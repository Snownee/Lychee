package snownee.lychee.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import snownee.lychee.client.gui.RenderElement;

public class CompoundElementRenderer extends RenderElement {
	private final List<RenderElement> elements = new ArrayList<>();

	public CompoundElementRenderer(Collection<? extends RenderElement> elements) {
		add(elements);
	}

	public CompoundElementRenderer add(Collection<? extends RenderElement> element) {
		elements.addAll(element);
		return this;
	}

	public CompoundElementRenderer add(RenderElement... element) {
		Collections.addAll(elements, element);
		return this;
	}

	@Override
	public void render(GuiGraphics graphics) {
		for (RenderElement element : elements) {
			element.render(graphics);
		}
	}
}
