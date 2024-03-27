package snownee.lychee.compat.rei.elements;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

public class InteractiveWidget extends WidgetWithBounds {

	private final Rectangle bounds;
	private boolean focused = false;
	private boolean focusable = true;
	private Point point;
	@Nullable
	private Function<InteractiveWidget, @Nullable Component[]> tooltip;
	@Nullable
	private BiConsumer<InteractiveWidget, Integer> onClick;

	public InteractiveWidget(Rectangle bounds) {
		this.bounds = bounds;
		point = new Point(bounds.getCenterX(), bounds.getMaxY());
	}

	public final Point getPoint() {
		return point;
	}

	public final void setPoint(Point point) {
		this.point = Objects.requireNonNull(point);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		if (isHovered(mouseX, mouseY)) {
			Component[] tooltip = getTooltipLines();
			if (tooltip != null) {
				if (!focused && containsMouse(mouseX, mouseY)) {
					Tooltip.create(tooltip).queue();
				} else if (focused) {
					Tooltip.create(point, tooltip).queue();
				}
			}
		}
	}

	@Nullable
	public final Component[] getTooltipLines() {
		if (tooltip == null) {
			return null;
		}
		return tooltip.apply(this);
	}

	public final void setTooltipFunction(@Nullable Function<InteractiveWidget, @Nullable Component[]> tooltip) {
		this.tooltip = tooltip;
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return Collections.emptyList();
	}

	@Override
	public Rectangle getBounds() {
		return bounds;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (isClickable() && containsMouse(mouseX, mouseY)) {
			Widgets.produceClickSound();
			onClick.accept(this, button);
			return true;
		}
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!isClickable() || !isFocusable() || !focused) {
			return false;
		}
		if (keyCode != 257 && keyCode != 32 && keyCode != 335) {
			return false;
		}
		Widgets.produceClickSound();
		if (onClick != null) {
			onClick.accept(this, 0);
		}
		return true;
	}

	public final boolean isClickable() {
		return getOnClick() != null;
	}

	@Nullable
	public final BiConsumer<InteractiveWidget, Integer> getOnClick() {
		return onClick;
	}

	public final void setOnClick(@Nullable BiConsumer<InteractiveWidget, Integer> onClick) {
		this.onClick = onClick;
	}

	public final boolean isFocusable() {
		return focusable;
	}

	public final void setFocusable(boolean focusable) {
		this.focusable = focusable;
	}

	public boolean isHovered(int mouseX, int mouseY) {
		return containsMouse(mouseX, mouseY) || focused;
	}
}
