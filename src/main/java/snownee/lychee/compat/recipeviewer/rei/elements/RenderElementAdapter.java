package snownee.lychee.compat.recipeviewer.rei.elements;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;

public class RenderElementAdapter extends WidgetWithBounds {
	private final RenderElement element;
	private final Rectangle bounds;

	public RenderElementAdapter(RenderElement element) {
		this.element = element;
		this.bounds = new Rectangle(element.x(), element.y(), element.width(), element.height());
	}

	@Override
	public @NotNull List<? extends GuiEventListener> children() {
		return List.of();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!(element instanceof GuiEventListener listener)) {
			return false;
		}
		return listener.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public Rectangle getBounds() {
		return bounds;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (element instanceof InteractiveRenderElement interactive && element.bounds.contains(mouseX, mouseY)) {
			var tooltip = interactive.getTooltip();
			if (tooltip != null) {
				Tooltip.create(new Point(mouseX, mouseY), tooltip).queue();
			}
		}
		element.render(guiGraphics, mouseX, mouseY, partialTick);
	}
}
