package snownee.lychee.client.gui;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;

public class SimpleRenderElement extends RenderElement {

	private final Function<RenderElement, BiConsumer<GuiGraphics, RenderElement>> renderable;

	public SimpleRenderElement(Function<RenderElement, BiConsumer<GuiGraphics, RenderElement>> renderable) {
		this.renderable = renderable;
	}

	public SimpleRenderElement(BiConsumer<GuiGraphics, RenderElement> renderable) {
		this.renderable = ignored -> renderable;
	}

	public SimpleRenderElement(ScreenElement renderable) {
		this.renderable = ignored -> (graphics, element) -> renderable.render(graphics);
	}

	@Override
	public void render(GuiGraphics graphics) {
		PoseStack pose = graphics.pose();
		pose.pushPose();
		pose.translate(x(), y(), z);
		renderable.apply(this).accept(graphics, this);
		pose.popPose();
	}
}
