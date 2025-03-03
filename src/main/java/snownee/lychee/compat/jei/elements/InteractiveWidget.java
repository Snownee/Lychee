package snownee.lychee.compat.jei.elements;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public class InteractiveWidget extends WidgetWithBounds {

	@Nullable
	private Function<InteractiveWidget, @Nullable List<Component>> tooltip;
	@Nullable
	private BiConsumer<InteractiveWidget, Integer> onClick;
	@Nullable
	private Renderable renderable;

	public InteractiveWidget(ScreenRectangle bounds) {
		super(bounds);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (isClickable() && containsMouse(mouseX, mouseY)) {
			produceClickSound();
			onClick.accept(this, button);
			return true;
		}
		return false;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		if (renderable != null) {
			renderable.render(graphics, mouseX, mouseY, delta);
		}
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
		if (containsMouse(mouseX, mouseY)) {
			@Nullable List<Component> lines = getTooltipLines();
			if (lines != null) {
				tooltip.addAll(lines);
			}
		}
	}

	@Nullable
	public final List<Component> getTooltipLines() {
		if (tooltip == null) {
			return null;
		}
		return tooltip.apply(this);
	}

	public final void setTooltipFunction(@Nullable Function<InteractiveWidget, @Nullable List<Component>> tooltip) {
		this.tooltip = tooltip;
	}

	public final boolean isClickable() {
		return onClick != null;
	}

	@Nullable
	public final BiConsumer<InteractiveWidget, Integer> getOnClick() {
		return onClick;
	}

	public final void setOnClick(@Nullable BiConsumer<InteractiveWidget, Integer> onClick) {
		this.onClick = onClick;
	}

	public void setRenderable(@Nullable Renderable renderable) {
		this.renderable = renderable;
	}
}
