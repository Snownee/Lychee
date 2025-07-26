package snownee.lychee.compat.recipeviewer.emi.element;

import dev.emi.emi.api.render.EmiRenderable;
import net.minecraft.client.gui.GuiGraphics;
import snownee.lychee.client.gui.RenderElement;

public class EmiRenderableAdapter implements EmiRenderable {
	private final RenderElement element;

	public EmiRenderableAdapter(RenderElement element) {
		this.element = element;
	}

	@Override
	public void render(GuiGraphics draw, int x, int y, float delta) {
		element.render(draw, x, y);
	}
}
