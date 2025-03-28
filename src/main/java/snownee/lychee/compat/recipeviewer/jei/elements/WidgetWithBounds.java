package snownee.lychee.compat.recipeviewer.jei.elements;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public abstract class WidgetWithBounds implements IRecipeWidget, IJeiGuiEventListener, IDrawable {
	private ScreenRectangle bounds;
	private ScreenPosition point;

	public WidgetWithBounds(ScreenRectangle bounds) {
		this.bounds = bounds;
		point = bounds.position();
	}

	public static void produceClickSound() {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	private static float partialTicks() {
		return Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
	}

	@Override
	public ScreenRectangle getArea() {
		return bounds;
	}

	public void setArea(ScreenRectangle bounds) {
		this.bounds = bounds;
		point = bounds.position();
	}

	@Override
	public ScreenPosition getPosition() {
		return point;
	}

	public void setPosition(ScreenPosition position) {
		setArea(new ScreenRectangle(position, bounds.width(), bounds.height()));
	}

	public boolean containsMouse(double mouseX, double mouseY) {
		ScreenRectangle area = getArea();
		return mouseX >= 0 && mouseY >= 0 && mouseX < area.width() && mouseY < area.height();
	}

	@Override
	public final void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
		ScreenPosition position = getPosition();
		guiGraphics.pose().translate(-position.x(), -position.y(), 0);
		render(guiGraphics, (int) mouseX + position.x(), (int) mouseY + position.y(), partialTicks());
	}

	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
	}

	@Override
	public final int getWidth() {
		return bounds.width();
	}

	@Override
	public final int getHeight() {
		return bounds.height();
	}

	@Override
	public final void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		boolean offset = xOffset != 0 || yOffset != 0;
		if (offset) {
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(xOffset, yOffset, 0);
		}
		render(guiGraphics, -1, -1, partialTicks());
		if (offset) {
			guiGraphics.pose().popPose();
		}
	}
}
