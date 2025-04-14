package snownee.lychee.compat.recipeviewer.jei.element;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;


public class RenderElementAdapter implements IRecipeWidget, IJeiGuiEventListener, IDrawable {
	private final RenderElement element;
	private final ScreenRectangle bounds;

	public RenderElementAdapter(RenderElement element) {
		this.element = element;
		this.bounds = new ScreenRectangle((int) element.x(), (int) element.y(), element.width(), element.height());
	}

	@Override
	public int getWidth() {
		return element.width();
	}

	@Override
	public int getHeight() {
		return element.height();
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		PoseStack pose = guiGraphics.pose();
		pose.pushPose();
		pose.translate(xOffset, yOffset, element.z());
		element.render(guiGraphics);
		pose.popPose();
	}

	@Override
	public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
		if (element instanceof InteractiveRenderElement interactive) {
			interactive.updateHoverState(element.x() + mouseX, element.y() + mouseY);
		}
		draw(guiGraphics, (int) -element.x(), (int) -element.y());
	}

	@Override
	public ScreenRectangle getArea() {
		return bounds;
	}

	@Override
	public ScreenPosition getPosition() {
		return bounds.position();
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
		if (!(element instanceof InteractiveRenderElement interactive) || !interactive.isHovered()) {
			return;
		}
		var components = interactive.getTooltip();
		if (components == null) {
			return;
		}
		tooltip.clear();
		tooltip.addAll(components);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!(element instanceof GuiEventListener listener)) {
			return false;
		}
		return listener.mouseClicked(-1, -1, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!(element instanceof GuiEventListener listener)) {
			return false;
		}
		return listener.mouseScrolled(-1, -1, scrollX, scrollY);
	}

	@Override
	public boolean keyPressed(double mouseX, double mouseY, int keyCode, int scanCode, int modifiers) {
		if (!(element instanceof GuiEventListener listener)) {
			return false;
		}
		return listener.keyPressed(keyCode, scanCode, modifiers);
	}
}
