package snownee.lychee.compat.rei;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.client.gui.ScreenElement;

public class ScreenElementWrapper extends WidgetWithBounds {

	public final Rectangle bounds = new Rectangle(16, 16);
	private final ScreenElement element;

	private ScreenElementWrapper(AllGuiTextures element) {
		this.element = element;
		bounds.width = element.width;
		bounds.height = element.height;
	}

	public ScreenElementWrapper(RenderElement element) {
		this.element = element;
		bounds.width = element.getWidth();
		bounds.height = element.getHeight();
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
	public @NotNull List<? extends GuiEventListener> children() {
		return Collections.emptyList();
	}

}