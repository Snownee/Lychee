package snownee.lychee.category;

import net.minecraft.client.gui.GuiGraphics;
import snownee.lychee.client.gui.RenderElement;

public class SpriteElementRenderer extends RenderElement {
	private final SpriteElement element;

	public SpriteElementRenderer(SpriteElement element) {
		this.element = element;
	}

	@Override
	public void render(GuiGraphics graphics) {
//		graphics.blitSprite(element.id());
	}
}
