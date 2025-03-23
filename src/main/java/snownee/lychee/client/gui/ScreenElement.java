package snownee.lychee.client.gui;

import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface ScreenElement {
	void render(GuiGraphics graphics, int x, int y);
}
