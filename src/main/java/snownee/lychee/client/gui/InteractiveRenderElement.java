package snownee.lychee.client.gui;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import snownee.lychee.util.VectorExtensions;
import snownee.lychee.util.ui.InputAction;

public class InteractiveRenderElement extends RenderElement implements WrapperRenderElement, GuiEventListener {
	private final @Nullable Function<InteractiveRenderElement, ScreenElement> renderable;
	private @Nullable Supplier<@Nullable List<Component>> onTooltip;
	private @Nullable BiPredicate<InputAction, InteractiveRenderElement> onInput;
	private boolean focused;
	private boolean withScissors;
	private boolean hovered;

	public InteractiveRenderElement(Function<InteractiveRenderElement, ScreenElement> renderable) {
		this.renderable = renderable;
	}

	public static InteractiveRenderElement create(ScreenElement element) {
		if (element instanceof InteractiveRenderElement interactiveElement) {
			return interactiveElement;
		}
		InteractiveRenderElement interactiveElement = new InteractiveRenderElement(ignored -> element);
		if (element instanceof RenderElement renderElement) {
			interactiveElement.at(renderElement.position).withSize(renderElement.size);
			renderElement.at(VectorExtensions.ZERO3F);
		}
		return interactiveElement;
	}

	public InteractiveRenderElement() {
		renderable = null;
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
		if (renderable == null) {
			return;
		}
		boolean withScissors = this.withScissors;
		if (withScissors) {
			Matrix4f matrix = graphics.pose().last().pose();
			Vector3f topLeft = matrix.transformPosition(new Vector3f());
			Vector3f bottomRight = matrix.transformPosition(new Vector3f(size.x(), size.y(), 0));
			graphics.enableScissor(
					(int) topLeft.x(),
					(int) topLeft.y(),
					(int) bottomRight.x(),
					(int) bottomRight.y());
		}
		graphics.pose().pushPose();
		graphics.pose().translate(x(), y(), z());
		renderable.apply(this).render(graphics);
		graphics.pose().popPose();
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
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (onInput != null && hovered && onInput.test(InputAction.mousePressed(button, mouseX, mouseY), this)) {
			produceClickSound();
			return true;
		}
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return onInput != null && onInput.test(InputAction.keyPressed(keyCode, scanCode, modifiers), this);
	}

	@Override
	public @Nullable ScreenElement getWrappedElement() {
		return renderable == null ? null : renderable.apply(this);
	}

	public void updateHoverState(double mouseX, double mouseY) {
		hovered = isMouseOver(mouseX, mouseY);
	}
}
