package snownee.lychee.client.gui;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.joml.Matrix3x2fStack;

import net.minecraft.client.gui.GuiGraphics;

public class SimpleRenderElement extends RenderElement {

	private final Function<RenderElement, BiConsumer<GuiGraphics, RenderElement>> renderable;

	public SimpleRenderElement(Function<RenderElement, BiConsumer<GuiGraphics, RenderElement>> renderable) {
		this.renderable = renderable;
	}

	public SimpleRenderElement(BiConsumer<GuiGraphics, RenderElement> renderable) {
		this.renderable = ignored -> renderable;
	}

	@Override
	public void render(GuiGraphics graphics) {
		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(x(), y());
		renderable.apply(this).accept(graphics, this);
		pose.popMatrix();
	}
}
