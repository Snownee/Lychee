package snownee.lychee.compat.rei;

import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import snownee.lychee.client.gui.ScreenElement;

// F. whatever. who cares?
public class LEntryWidget extends EntryWidget {

	private ScreenElement bg;
	private UnaryOperator<@Nullable Tooltip> tooltipCallback;

	public LEntryWidget(Point point) {
		super(point);
	}

	public LEntryWidget(Rectangle bounds) {
		super(bounds);
	}

	@Override
	protected void drawBackground(PoseStack matrices, int mouseX, int mouseY, float delta) {
		if (background) {
			super.drawBackground(matrices, mouseX, mouseY, delta);
		} else if (bg != null) {
			Rectangle rect = getBounds();
			bg.render(matrices, rect.x, rect.y);
		}
	}

	public void background(ScreenElement bg) {
		disableBackground();
		this.bg = bg;
	}

	@Override
	public @Nullable Tooltip getCurrentTooltip(TooltipContext ctx) {
		Tooltip tooltip = super.getCurrentTooltip(ctx);
		if (tooltipCallback != null) {
			tooltip = tooltipCallback.apply(tooltip);
		}
		return tooltip;
	}

	public void addTooltipCallback(UnaryOperator<@Nullable Tooltip> tooltipCallback) {
		this.tooltipCallback = tooltipCallback;
	}

}
