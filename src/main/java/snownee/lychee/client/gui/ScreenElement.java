package snownee.lychee.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

@FunctionalInterface
public interface ScreenElement {
	void render(GuiGraphicsExtractor graphics);
}
