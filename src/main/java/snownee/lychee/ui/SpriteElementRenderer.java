package snownee.lychee.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.client.gui.LycheeGuiGraphics;
import snownee.lychee.client.gui.RenderElement;

public class SpriteElementRenderer extends RenderElement {
	private final ResourceLocation id;
	private final float scale;

	public SpriteElementRenderer(ResourceLocation id) {
		this(id, 1F);
	}

	public SpriteElementRenderer(ResourceLocation id, float scale) {
		this.id = id;
		this.scale = scale;
	}

	@Override
	public void render(GuiGraphics graphics) {
		int width = (int) (width() * scale);
		int height = (int) (height() * scale);
		float xOff = (width() - width) / 2F;
		int x = (int) (x() + xOff);
		float yOff = (height() - height) / 2F;
		int y = (int) (y() + yOff);
		((LycheeGuiGraphics) graphics).lychee$setRenderType(LycheeGuiGraphics::guiTextured);
		graphics.blitSprite(id, x, y, (int) z(), width, height);
		((LycheeGuiGraphics) graphics).lychee$setRenderType(null);
	}

	public static SpriteElementRenderer create(SpriteElement element) {
		return new SpriteElementRenderer(element.id(), element.scale());
	}
}
