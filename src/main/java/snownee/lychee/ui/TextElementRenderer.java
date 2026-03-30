package snownee.lychee.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import snownee.lychee.client.gui.RenderElement;


public class TextElementRenderer extends RenderElement {
	public final Component text;
	public final Component darkText;
	public Font font;
	public int lightModeColor = TextElement.DEFAULT_LIGHT_MODE_COLOR;
	public int darkModeColor = TextElement.DEFAULT_DARK_MODE_COLOR;
	public boolean shadow;
	public boolean centered;

	public TextElementRenderer(Component text) {
		this(text, text);
	}

	public TextElementRenderer(Component text, Component darkText) {
		this.text = text;
		this.darkText = darkText;
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
	public void render(GuiGraphicsExtractor graphics) {
		if (!centered) {
			graphics.text(font, text, (int) x(), (int) y(), lightModeColor, shadow);
		} else {
			graphics.text(font, text, (int) (x() - (float) font.width(text) / 2), (int) y(), lightModeColor, shadow);
		}
	}

	public static TextElementRenderer create(TextElement element) {
		TextElementRenderer renderer = new TextElementRenderer(element.text(), element.darkText().orElse(element.text()))
				.color(element.color(), element.darkColor());
		if (element.centered()) {
			renderer.centered();
		}
		if (element.shadow()) {
			renderer.shadow();
		}
		return renderer;
	}
}
