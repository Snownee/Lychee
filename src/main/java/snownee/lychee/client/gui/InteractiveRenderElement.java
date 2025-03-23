package snownee.lychee.client.gui;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class InteractiveRenderElement extends RenderElement implements GuiEventListener {
	private final Function<InteractiveRenderElement, ScreenElement> renderable;
	private boolean focused;
	private @Nullable Supplier<List<Component>> onTooltip = null;
	private @Nullable Consumer<Integer> onClick = null;

	public InteractiveRenderElement(Function<InteractiveRenderElement, ScreenElement> renderable) {
		this.renderable = renderable;
	}

	public InteractiveRenderElement(ScreenElement renderable) {
		this((ignored) -> renderable);
	}

	public InteractiveRenderElement() {
		this(ignored -> RenderElement.EMPTY);
	}

	public static void produceClickSound() {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	@Override
	public boolean isFocused() {
		return focused;
	}

	@Override
	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	@Override
	public void render(GuiGraphics graphics) {
		renderable.apply(this).render(graphics, x(), y());
	}

	public InteractiveRenderElement onTooltip(@Nullable Supplier<List<Component>> onTooltip) {
		this.onTooltip = onTooltip;
		return this;
	}

	public InteractiveRenderElement onClick(@Nullable Consumer<Integer> onClick) {
		this.onClick = onClick;
		return this;
	}

	public List<Component> getTooltip() {
		if (onTooltip == null) {
			return null;
		}
		return onTooltip.get();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (onClick != null && bounds.contains((int) mouseX, (int) mouseY)) {
			produceClickSound();
			onClick.accept(button);
			return true;
		}
		return false;
	}
}
