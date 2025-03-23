package snownee.lychee.ui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.client.gui.RenderElement;

public class SpriteElementRenderer extends RenderElement {
	private final ResourceLocation id;
	private final float scale;

	public SpriteElementRenderer(ResourceLocation id, Rect2i bounds, int z, float scale) {
		this.id = id;
		this.bounds = bounds;
		this.z = z;
		this.scale = scale;
	}

	@Override
	public void render(GuiGraphics graphics) {
		int width = (int) (this.width() * scale);
		int height = (int) (this.height() * scale);
		float xOff = (this.width() - width) / 2F;
		int x = (int) (this.x() + xOff);
		float yOff = (this.height() - height) / 2F;
		int y = (int) (this.y() + yOff);
		graphics.blitSprite(id, x, y, z, width, height);
	}
}
