package snownee.lychee.category;

import net.minecraft.client.gui.GuiGraphics;
import snownee.lychee.client.gui.RenderElement;

public class SpriteElementRenderer extends RenderElement {
	private final SpriteElement element;
	private final int zOffset;

	public SpriteElementRenderer(SpriteElement element, int x, int y, int zOffset, int width, int height) {
		this.element = element;
		this.x = x;
		this.y = y;
		this.zOffset = zOffset;
		this.width = width;
		this.height = height;
	}

	public SpriteElementRenderer(SpriteElement element, int x, int y, int width, int height) {
		this.element = element;
		this.x = x;
		this.y = y;
		this.zOffset = 0;
		this.width = width;
		this.height = height;
	}

	@Override
	public void render(GuiGraphics graphics) {
		graphics.blitSprite(element.id(), (int) x, (int) y, zOffset, width, height);
	}
}
