package snownee.lychee.compat.recipeviewer.rei.element;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.impl.client.gui.widget.EntryWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import snownee.lychee.client.gui.ScreenElement;

public class LycheeEntryWidget extends EntryWidget {

	private ScreenElement bg;
	private List<Component> extraTooltips = List.of();

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
			graphics.pose().pushPose();
			graphics.pose().translate(rect.x, rect.y, 0);
			bg.render(graphics);
			graphics.pose().popPose();
		}
	}

	public void background(ScreenElement bg) {
		disableBackground();
		this.bg = bg;
	}

	@Override
	public @Nullable Tooltip getCurrentTooltip(TooltipContext context) {
		Tooltip tooltip = super.getCurrentTooltip(context);
		if (!extraTooltips.isEmpty()) {
			if (tooltip == null) {
				tooltip = Tooltip.create();
			}
			tooltip.addAllTexts(extraTooltips);
		}
		return tooltip;
	}

	public void setExtraTooltips(List<Component> tooltips) {
		extraTooltips = tooltips;
	}
}
