package snownee.lychee.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.client.gui.RenderElement;

@NotNullByDefault
public class TextElementRenderer extends RenderElement {
	public final Component text;
	public Font font;
	public int lightModeColor = 0xFF666666;
	public int darkModeColor = 0xFFBBBBBB;
	public boolean shadow;
	public boolean centered;

	public TextElementRenderer(Component text) {
		this.text = text;
		this.font = Minecraft.getInstance().font;
		this.shadow = false;
	}

	public TextElementRenderer font(Font font) {
		this.font = font;
		return this;
	}

	public TextElementRenderer color(int lightModeColor, int darkModeColor) {
		this.lightModeColor = lightModeColor;
		this.darkModeColor = darkModeColor;
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
			graphics.drawString(font, text, (int) x(), (int) y(), lightModeColor, shadow);
		} else {
			graphics.drawString(font, text, (int) (x() - (float) font.width(text) / 2), (int) y(), lightModeColor, shadow);
		}
	}
}
