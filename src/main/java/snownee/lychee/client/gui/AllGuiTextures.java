package snownee.lychee.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.Lychee;
import snownee.lychee.ui.SpriteElement;
import snownee.lychee.ui.SpriteElementRenderer;

public enum AllGuiTextures implements ScreenElement {
	DOWN_ARROW("down_arrow", 20, 20),
	QUESTION_MARK("unknown", 12, 17), // 12, 16
	SHADOW("shadow", 52, 13), // 52, 11
	LIGHT_SHADOW("light_shadow", 52, 13),
	INFO("info", 16, 16),
	LEFT_CLICK("left_click", 16, 16),
	RIGHT_CLICK("right_click", 16, 16);

	private final SpriteElementRenderer renderer;
	public final int width, height;

	AllGuiTextures(String id, int width, int height) {
		this(Lychee.id(id), width, height);
	}

	AllGuiTextures(ResourceLocation id, int width, int height) {
		this.renderer = new SpriteElementRenderer(new SpriteElement(id), 0, 0, 0, width, height, 1);
		this.width = width;
		this.height = height;
	}

	@Override
	public void render(GuiGraphics graphics, int x, int y) {
		renderer.render(graphics, x, y);
	}

}
