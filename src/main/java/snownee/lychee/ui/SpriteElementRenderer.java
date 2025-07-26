package snownee.lychee.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.client.gui.LycheeGuiGraphics;
import snownee.lychee.client.gui.RenderElement;

public class SpriteElementRenderer extends RenderElement {
	public final ResourceLocation id;
	public final float scale;

	public SpriteElementRenderer(ResourceLocation id) {
		this(id, 1F);
	}

	public SpriteElementRenderer(ResourceLocation id, float scale) {
		this.id = id;
		this.scale = scale;
	}

	@Override
	public void render(GuiGraphics graphics) {
		int width = Math.round(width() * scale);
		int height = Math.round(height() * scale);
		float xOff = (width() - width) * 0.5F;
		int x = Math.round(x() + xOff);
		float yOff = (height() - height) * 0.5F;
		int y = Math.round(y() + yOff);
		((LycheeGuiGraphics) graphics).lychee$setRenderType(LycheeGuiGraphics::guiTextured);
		graphics.blitSprite(id, x, y, (int) z(), width, height);
		((LycheeGuiGraphics) graphics).lychee$setRenderType(null);
	}

	public static SpriteElementRenderer create(SpriteElement element) {
		return new SpriteElementRenderer(element.id(), element.scale());
	}
}
