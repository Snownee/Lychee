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
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.util.VectorExtensions;

@NotNullByDefault
public class InteractiveRenderElement extends RenderElement implements GuiEventListener {
	private final @Nullable Function<InteractiveRenderElement, ScreenElement> renderable;
	private @Nullable Supplier<@Nullable List<Component>> onTooltip;
	private @Nullable Consumer<Integer> onClick;
	private boolean focused;

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
		graphics.pose().pushPose();
		graphics.pose().translate(x(), y(), z());
		renderable.apply(this).render(graphics);
		graphics.pose().popPose();
	}

	public InteractiveRenderElement onTooltip(@Nullable Supplier<@Nullable List<Component>> onTooltip) {
		this.onTooltip = onTooltip;
		return this;
	}

	public InteractiveRenderElement onClick(@Nullable Consumer<Integer> onClick) {
		this.onClick = onClick;
		return this;
	}

	@Nullable
	public List<Component> getTooltip() {
		if (onTooltip == null) {
			return null;
		}
		return onTooltip.get();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (onClick != null && containsMouse(mouseX, mouseY)) {
			produceClickSound();
			onClick.accept(button);
			return true;
		}
		return false;
	}
}
