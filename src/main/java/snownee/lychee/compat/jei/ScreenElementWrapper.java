package snownee.lychee.compat.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.client.gui.ScreenElement;

public class ScreenElementWrapper implements IDrawable {

	public final Rect2i bounds = new Rect2i(0, 0, 16, 16);
	private final ScreenElement element;

	public ScreenElementWrapper(AllGuiTextures element) {
		this.element = element;
		bounds.setWidth(element.width);
		bounds.setHeight(element.height);
	}

	public ScreenElementWrapper(RenderElement element) {
		this.element = element;
		bounds.setWidth(element.getWidth());
		bounds.setHeight(element.getHeight());
	}

	@Override
	public int getWidth() {
		return bounds.getWidth();
	}

	@Override
	public int getHeight() {
		return bounds.getHeight();
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		element.render(guiGraphics, bounds.getX() + xOffset, bounds.getY() + yOffset);
	}
}