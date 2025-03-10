package snownee.lychee.category;

import net.minecraft.client.gui.GuiGraphics;
import snownee.lychee.client.gui.RenderElement;

public class SpriteElementRenderer extends RenderElement {
	private final SpriteElement element;
	private final int textureWidth;
	private final int textureHeight;
	private final int uPosition;
	private final int vPosition;
	private final int x;
	private final int y;
	private final int blitOffset;
	private final int uWidth;
	private final int vHeight;

	public SpriteElementRenderer(
			SpriteElement element,
			int textureWidth,
			int textureHeight,
			int uPosition,
			int vPosition,
			int x,
			int y,
			int blitOffset,
			int uWidth,
			int vHeight
	) {
		this.element = element;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.uPosition = uPosition;
		this.vPosition = vPosition;
		this.x = x;
		this.y = y;
		this.blitOffset = blitOffset;
		this.uWidth = uWidth;
		this.vHeight = vHeight;
	}

	@Override
	public void render(GuiGraphics graphics) {
		graphics.blitSprite(element.id(), textureWidth, textureHeight, uPosition, vPosition, x, y, blitOffset, uWidth, vHeight);
	}
}
