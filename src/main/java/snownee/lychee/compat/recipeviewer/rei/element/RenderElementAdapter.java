package snownee.lychee.compat.recipeviewer.rei.element;

import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector2fc;


import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;

public class RenderElementAdapter extends WidgetWithBounds {
	private final RenderElement element;
	private final Rectangle bounds;
	private final Vector2f startPoint;

	public RenderElementAdapter(RenderElement element) {
		this.element = element;
		this.bounds = new Rectangle(element.x(), element.y(), element.width(), element.height());
		this.startPoint = new Vector2f(0, 0);
	}

	public RenderElementAdapter(RenderElement element, Point startPoint) {
		this(element);
		this.startPoint.set(startPoint.x, startPoint.y);
		bounds.x += startPoint.x;
		bounds.y += startPoint.y;
	}

	public RenderElementAdapter(RenderElement element, Vector2fc startPoint) {
		this(element);
		this.startPoint.set(startPoint);
		bounds.x += (int) startPoint.x();
		bounds.y += (int) startPoint.y();
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return List.of();
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (!(element instanceof GuiEventListener listener)) {
			return false;
		}
		double relMouseX = event.x() - startPoint.x();
		double relMouseY = event.y() - startPoint.y();
		return listener.mouseClicked(new MouseButtonEvent(relMouseX, relMouseY, event.buttonInfo()), doubleClick);
	}


	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!(element instanceof GuiEventListener listener)) {
			return false;
		}
		double relMouseX = mouseX - startPoint.x();
		double relMouseY = mouseY - startPoint.y();
		return listener.mouseScrolled(relMouseX, relMouseY, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (!(element instanceof GuiEventListener listener)) {
			return false;
		}
		return listener.keyPressed(event);
	}

	@Override
	public Rectangle getBounds() {
		return bounds;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		double relMouseX = mouseX - startPoint.x();
		double relMouseY = mouseY - startPoint.y();
		if (element instanceof InteractiveRenderElement interactive) {
			interactive.updateHoverState(relMouseX, relMouseY);
			if (interactive.isHovered()) {
				var tooltip = interactive.getTooltip();
				if (tooltip != null) {
					Tooltip.create(tooltip).queue();
				}
			}
		}
		var pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(bounds.x - element.x(), bounds.y - element.y());
		element.render(graphics);
		pose.popMatrix();
	}
}
