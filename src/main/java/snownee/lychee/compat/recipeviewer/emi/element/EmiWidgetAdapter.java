package snownee.lychee.compat.recipeviewer.emi.element;

import java.util.List;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;

public class EmiWidgetAdapter extends Widget {
	private final RenderElement element;
	private final Bounds bounds;

	public EmiWidgetAdapter(RenderElement element) {
		this.element = element;
		this.bounds = new Bounds((int) element.x(), (int) element.y(), element.width(), element.height());
	}

	@Override
	public Bounds getBounds() {
		return bounds;
	}

	@Override
	public void render(GuiGraphics draw, int mouseX, int mouseY, float delta) {
		if (element instanceof InteractiveRenderElement interactive) {
			interactive.updateHoverState(mouseX, mouseY);
		}
		element.render(draw, mouseX, mouseY, delta);
	}

	@Override
	public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
		if (element instanceof InteractiveRenderElement interactive && interactive.getTooltip() != null) {
			return interactive.getTooltip().stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList();
		}
		return List.of();
	}

	@Override
	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		if (element instanceof InteractiveRenderElement interactive) {
			return interactive.mouseClicked(mouseX, mouseY, button);
		}
		return false;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (element instanceof InteractiveRenderElement interactive) {
			return interactive.keyPressed(keyCode, scanCode, modifiers);
		}
		return false;
	}
}
