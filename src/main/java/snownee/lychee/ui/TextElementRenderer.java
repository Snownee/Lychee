package snownee.lychee.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import snownee.lychee.client.gui.RenderElement;

public class TextElementRenderer extends RenderElement {
	private final Component text;
	private Font font;
	private int color;
	private boolean shadow;
	private boolean centered;

	public TextElementRenderer(Component text) {
		this.text = text;
		this.font = Minecraft.getInstance().font;
		this.color = 0XFFFFFFFF;
		this.shadow = false;
	}

	public TextElementRenderer(Component text, Font font, int color, boolean shadow, boolean centered) {
		this.text = text;
		this.font = font;
		this.color = color;
		this.shadow = shadow;
		this.centered = centered;
	}

	public TextElementRenderer font(Font font) {
		this.font = font;
		return this;
	}

	public TextElementRenderer color(int color) {
		this.color = color;
		return this;
	}

	public TextElementRenderer shadow() {
		this.shadow = true;
		return this;
	}

	public TextElementRenderer centered() {
		this.centered = true;
		return this;
	}

	@Override
	public void render(GuiGraphics graphics) {
		if (!centered) {
			graphics.drawString(font, text, (int) x(), (int) y(), color, shadow);
		} else {
			graphics.drawString(font, text, (int) (x() - (float) font.width(text) / 2), (int) y(), color, shadow);
		}
	}
}
