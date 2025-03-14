package snownee.lychee.category;

import net.minecraft.client.gui.GuiGraphics;
import snownee.lychee.client.gui.RenderElement;

public class SpriteElementRenderer extends RenderElement {
	private final SpriteElement element;
	private final float scale;

	public SpriteElementRenderer(SpriteElement element, int x, int y, int z, int width, int height, float scale) {
		this.element = element;
		this.x = x;
		this.y = y;
		this.z = z;
		this.width = width;
		this.height = height;
		this.scale = scale;
	}

	@Override
	public void render(GuiGraphics graphics) {
		int width = (int) (this.width * scale);
		int height = (int) (this.height * scale);
		float xOff = (this.width - width) / 2F;
		int x = (int) (this.x + xOff);
		float yOff = (this.height - height) / 2F;
		int y = (int) (this.y + yOff);
		graphics.blitSprite(element.id(), x, y, (int) z, width, height);
	}
}
