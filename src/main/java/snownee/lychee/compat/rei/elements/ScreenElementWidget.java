package snownee.lychee.compat.rei.elements;

import java.util.Collections;
import java.util.List;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.client.gui.ScreenElement;

@NotNullByDefault
public class ScreenElementWidget extends WidgetWithBounds {

	public final Rectangle bounds = new Rectangle(16, 16);
	public final ScreenElement element;

	public ScreenElementWidget(AllGuiTextures element) {
		this.element = element;
		bounds.width = element.width;
		bounds.height = element.height;
	}

	public ScreenElementWidget(RenderElement element) {
		this.element = element;
		bounds.width = element.width();
		bounds.height = element.height();
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		element.render(graphics, bounds.x, bounds.y);
	}

	@Override
	public Rectangle getBounds() {
		return bounds;
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return Collections.emptyList();
	}

}