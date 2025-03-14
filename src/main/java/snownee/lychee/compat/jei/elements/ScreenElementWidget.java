package snownee.lychee.compat.jei.elements;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.client.gui.ScreenElement;

@NotNullByDefault
public class ScreenElementWidget extends WidgetWithBounds {

	private final ScreenElement element;

	public ScreenElementWidget(ScreenElement element) {
		super(new ScreenRectangle(0, 0, 16, 16));
		this.element = element;
	}

	public ScreenElementWidget(RenderElement element) {
		super(new ScreenRectangle(0, 0, element.getWidth(), element.getHeight()));
		this.element = element;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		element.render(graphics, getArea().left(), getArea().top());
	}
}