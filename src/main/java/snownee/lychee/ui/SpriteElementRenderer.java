package snownee.lychee.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import snownee.lychee.client.gui.RenderElement;

public class SpriteElementRenderer extends RenderElement {
	private final SpriteElement element;
	private final float scale;

	public SpriteElementRenderer(SpriteElement element, Rect2i bounds, int z, float scale) {
		this.element = element;
		this.bounds = bounds;
		this.z = z;
		this.scale = scale;
	}

	@Override
	public void render(GuiGraphics graphics) {
		int width = (int) (this.getWidth() * scale);
		int height = (int) (this.getHeight() * scale);
		float xOff = (this.getWidth() - width) / 2F;
		int x = (int) (this.getX() + xOff);
		float yOff = (this.getHeight() - height) / 2F;
		int y = (int) (this.getY() + yOff);
		graphics.blitSprite(element.id(), x, y, z, width, height);
	}
}
