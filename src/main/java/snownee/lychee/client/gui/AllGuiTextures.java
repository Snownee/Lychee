package snownee.lychee.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import snownee.kiwi.util.Color;
import snownee.lychee.Lychee;

@Deprecated //TODO use sprites
public enum AllGuiTextures implements ScreenElement {
	// JEI
	JEI_DOWN_ARROW(0, 21, 18, 14),
	JEI_QUESTION_MARK(0, 178, 12, 16),
	JEI_SHADOW(0, 56, 52, 11),
	INFO(240, 0, 16, 16),
	LEFT_CLICK(192, 0, 16, 16),
	RIGHT_CLICK(224, 0, 16, 16);

	public final ResourceLocation location;
	public final int width, height;
	public final int startX, startY;

	AllGuiTextures(int startX, int startY, int width, int height) {
		this(Lychee.ID, "jei/widgets", startX, startY, width, height);
	}

	AllGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
		this.location = ResourceLocation.fromNamespaceAndPath(namespace, "textures/gui/" + location + ".png");
		this.width = width;
		this.height = height;
		this.startX = startX;
		this.startY = startY;
	}

	public void bind() {
		RenderSystem.setShaderTexture(0, location);
	}

	@Override
	public void render(GuiGraphics graphics, int x, int y) {
		graphics.blit(location, x, y, startX, startY, width, height);
	}

	public void render(GuiGraphics graphics, int x, int y, Color c) {
		bind();
		UIRenderHelper.drawColoredTexture(graphics, c, x, y, startX, startY, width, height);
	}

}
