package snownee.lychee.compat.rei.elements;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import net.minecraft.client.gui.GuiGraphics;
import snownee.lychee.client.gui.ScreenElement;

public class LycheeEntryWidget extends EntryWidget {

	private ScreenElement bg;

	public LycheeEntryWidget(Point point) {
		super(point);
	}

	public LycheeEntryWidget(Rectangle bounds) {
		super(bounds);
	}

	@Override
	protected void drawBackground(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		if (background) {
			super.drawBackground(graphics, mouseX, mouseY, delta);
		} else if (bg != null) {
			Rectangle rect = getBounds();
			bg.render(graphics, rect.x, rect.y);
		}
	}

	public void background(ScreenElement bg) {
		disableBackground();
		this.bg = bg;
	}

}
