package snownee.lychee.client.gui;

import java.util.function.BiConsumer;

import org.joml.Matrix3x2fStack;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public class SimpleRenderElement extends RenderElement {

	private final BiConsumer<GuiGraphicsExtractor, RenderElement> renderable;

	public SimpleRenderElement(BiConsumer<GuiGraphicsExtractor, RenderElement> renderable) {
		this.renderable = renderable;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics) {
		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(x(), y());
		renderable.accept(graphics, this);
		pose.popMatrix();
	}
}
