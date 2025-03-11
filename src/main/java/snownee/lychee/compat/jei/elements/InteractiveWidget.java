package snownee.lychee.compat.jei.elements;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public class InteractiveWidget implements IRecipeWidget, IJeiGuiEventListener {

	private final ScreenRectangle bounds;
	private ScreenPosition point;
	@Nullable
	private Function<InteractiveWidget, @Nullable List<Component>> tooltip;
	@Nullable
	private BiConsumer<InteractiveWidget, Integer> onClick;
	@Nullable
	private Renderable renderable;
	private boolean clearTooltip = true;

	public InteractiveWidget(ScreenRectangle bounds) {
		this.bounds = bounds;
		point = bounds.position();
	}

	public static void produceClickSound() {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	@Override
	public ScreenRectangle getArea() {
		return bounds;
	}

	@Override
	public ScreenPosition getPosition() {
		return point;
	}

	public void setPosition(ScreenPosition position) {
		this.point = position;
	}

	public InteractiveWidget(ScreenRectangle bounds, boolean clearTooltip) {
		super(bounds);
		this.clearTooltip = clearTooltip;
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
	public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
		if (containsMouse(mouseX, mouseY)) {
			@Nullable List<Component> lines = getTooltipLines();
			if (lines != null) {
				if (clearTooltip) {
					tooltip.clear();
				}
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

	@Nullable
	public final BiConsumer<InteractiveWidget, Integer> getOnClick() {
		return onClick;
	}

	public final void setOnClick(@Nullable BiConsumer<InteractiveWidget, Integer> onClick) {
		this.onClick = onClick;
	}

	public final boolean isClickable() {
		return onClick != null;
	}

	public boolean containsMouse(double mouseX, double mouseY) {
		ScreenRectangle area = getArea();
		return mouseX >= 0 && mouseY >= 0 && mouseX < area.width() && mouseY < area.height();
	}

}
