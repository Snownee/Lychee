package snownee.lychee.compat.recipeviewer.emi.element;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import snownee.lychee.client.gui.RenderElement;

public class EmiWidgetAdapter extends Widget {
	private final RenderElement element;
	private final Bounds bounds;

	public EmiWidgetAdapter(RenderElement element) {
		this.element = element;
		this.bounds = new Bounds((int) element.x(), (int) element.y(), element.width(), element.height());
	}

	@Override
	public Bounds getBounds() {
		return bounds;
	}

	@Override
	public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
		element.render(draw, mouseX, mouseY, delta);
	}
}
