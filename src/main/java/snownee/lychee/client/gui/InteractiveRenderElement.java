package snownee.lychee.client.gui;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import snownee.lychee.util.ui.InputAction;

public class InteractiveRenderElement extends RenderElement implements WrapperRenderElement, GuiEventListener {
	private final @Nullable Function<InteractiveRenderElement, @Nullable ScreenElement> renderable;
	private final @Nullable Consumer<InteractiveRenderElement> onFrame;
	private @Nullable Supplier<@Nullable List<Component>> onTooltip;
	private @Nullable BiPredicate<InputAction, InteractiveRenderElement> onInput;
	public boolean visible = true;
	private boolean focused;
	private boolean withScissors;
	private boolean hovered;

	public InteractiveRenderElement(
			Function<InteractiveRenderElement, @Nullable ScreenElement> renderable,
			@Nullable Consumer<InteractiveRenderElement> onFrame) {
		this.renderable = renderable;
		this.onFrame = onFrame;
	}

	public InteractiveRenderElement() {
		renderable = null;
		onFrame = null;
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
	public void render(GuiGraphicsExtractor graphics) {
		if (renderable == null) {
			return;
		}
		ScreenElement element = renderable.apply(this);
		if (element == null) {
			return;
		}
		if (onFrame != null) {
			onFrame.accept(this);
		}
		if (!visible) {
			return;
		}
		boolean withScissors = this.withScissors;
		if (withScissors) {
			graphics.enableScissor(0, 0, size.x(), size.y());
		}
		graphics.pose().pushMatrix();
		graphics.pose().translate(x(), y());
		element.render(graphics);
		graphics.pose().popMatrix();
		if (withScissors) {
			graphics.disableScissor();
		}
	}

	public InteractiveRenderElement onTooltip(@Nullable Supplier<@Nullable List<Component>> onTooltip) {
		this.onTooltip = onTooltip;
		return this;
	}

	public InteractiveRenderElement onInput(@Nullable BiPredicate<InputAction, @Nullable InteractiveRenderElement> onInput) {
		this.onInput = onInput;
		return this;
	}

	public InteractiveRenderElement withScissors(boolean withScissors) {
		this.withScissors = withScissors;
		return this;
	}

	@Nullable
	public List<Component> getTooltip() {
		if (onTooltip == null) {
			return null;
		}
		return onTooltip.get();
	}

	public @Nullable BiPredicate<InputAction, @Nullable InteractiveRenderElement> getOnInput() {
		return onInput;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return containsMouse(mouseX, mouseY);
	}

	public boolean isHovered() {
		return hovered;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (onInput != null && hovered && onInput.test(InputAction.mousePressed(event), this)) {
			produceClickSound();
			return true;
		}
		return false;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		return onInput != null && onInput.test(InputAction.keyPressed(event), this);
	}

	@Override
	public @Nullable ScreenElement getWrappedElement() {
		return renderable == null ? null : renderable.apply(this);
	}

	public void updateHoverState(double mouseX, double mouseY) {
		hovered = isMouseOver(mouseX, mouseY);
	}
}
