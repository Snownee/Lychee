package snownee.lychee.compat.rei;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

public class ReactiveWidget extends WidgetWithBounds {

	private boolean focused = false;
	private boolean focusable = true;
	private final Rectangle bounds;
	private Point point;
	@Nullable
	private Function<ReactiveWidget, @Nullable Component[]> tooltip;
	@Nullable
	private BiConsumer<ReactiveWidget, Integer> onClick;

	public ReactiveWidget(Rectangle bounds) {
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
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		if (isHovered(mouseX, mouseY)) {
			Component[] tooltip = getTooltipLines();
			if (tooltip != null) {
				if (!focused && containsMouse(mouseX, mouseY))
					Tooltip.create(tooltip).queue();
				else if (focused)
					Tooltip.create(point, tooltip).queue();
			}
		}
	}

	@Nullable
	public final Component[] getTooltipLines() {
		if (tooltip == null)
			return null;
		return tooltip.apply(this);
	}

	public final void setTooltipFunction(@Nullable Function<ReactiveWidget, @Nullable Component[]> tooltip) {
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
	public boolean keyPressed(int int_1, int int_2, int int_3) {
		if (!isClickable() || !isFocusable() || !focused)
			return false;
		if (int_1 != 257 && int_1 != 32 && int_1 != 335)
			return false;
		Widgets.produceClickSound();
		if (onClick != null)
			onClick.accept(this, 0);
		return true;
	}

	public final boolean isClickable() {
		return getOnClick() != null;
	}

	@Nullable
	public final BiConsumer<ReactiveWidget, Integer> getOnClick() {
		return onClick;
	}

	public final void setOnClick(@Nullable BiConsumer<ReactiveWidget, Integer> onClick) {
		this.onClick = onClick;
	}

	@Override
	public boolean changeFocus(boolean boolean_1) {
		if (!isClickable() || !isFocusable())
			return false;
		this.focused = !this.focused;
		return true;
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
