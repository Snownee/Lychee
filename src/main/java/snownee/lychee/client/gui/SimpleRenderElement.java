package snownee.lychee.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphics;

public class SimpleRenderElement extends RenderElement {

	private final ScreenElement renderable;

	public SimpleRenderElement(ScreenElement renderable) {
		this.renderable = renderable;
	}

	@Override
	public void render(GuiGraphics graphics) {
		PoseStack pose = graphics.pose();
		pose.pushPose();
		pose.translate(0, 0, z);
		renderable.render(graphics, x(), y());
		pose.popPose();
	}
}
