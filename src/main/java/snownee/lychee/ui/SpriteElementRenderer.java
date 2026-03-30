package snownee.lychee.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import snownee.lychee.client.gui.RenderElement;

public class SpriteElementRenderer extends RenderElement {
	public final Identifier id;
	public final float scale;

	public SpriteElementRenderer(Identifier id) {
		this(id, 1F);
	}

	public SpriteElementRenderer(Identifier id, float scale) {
		this.id = id;
		this.scale = scale;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics) {
		int width = Math.round(width() * scale);
		int height = Math.round(height() * scale);
		float xOff = (width() - width) * 0.5F;
		int x = Math.round(x() + xOff);
		float yOff = (height() - height) * 0.5F;
		int y = Math.round(y() + yOff);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, id, x, y, width, height);
	}

	public static SpriteElementRenderer create(SpriteElement element) {
		return new SpriteElementRenderer(element.id(), element.scale());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		render(graphics);
	}
}
