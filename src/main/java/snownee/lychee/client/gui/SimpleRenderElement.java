package snownee.lychee.client.gui;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;

public class SimpleRenderElement extends RenderElement {

	private final Function<RenderElement, ScreenElement> renderable;

	public SimpleRenderElement(Function<RenderElement, ScreenElement> renderable) {
		this.renderable = renderable;
	}

	public SimpleRenderElement(ScreenElement renderable) {
		this.renderable = ignored -> renderable;
	}

	@Override
	public void render(GuiGraphics graphics) {
		PoseStack pose = graphics.pose();
		pose.pushPose();
		pose.translate(0, 0, z);
		renderable.apply(this).render(graphics, x(), y());
		pose.popPose();
	}
}
