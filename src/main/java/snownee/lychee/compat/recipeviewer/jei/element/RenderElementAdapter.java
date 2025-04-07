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
		this.bounds = new ScreenRectangle(0, 0, element.width(), element.height());
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
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(xOffset, yOffset, 0);
		element.render(guiGraphics);
		guiGraphics.pose().popPose();
	}

	@Override
	public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
		PoseStack pose = guiGraphics.pose();
		pose.pushPose();
		pose.translate(bounds.left(), bounds.top(), 0);
		element.render(guiGraphics);
		pose.popPose();
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
		if (!(element instanceof InteractiveRenderElement interactive) || !interactive.containsMouse(mouseX, mouseY)) {
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
		return listener.mouseClicked(mouseX, mouseY, button);
	}
}
