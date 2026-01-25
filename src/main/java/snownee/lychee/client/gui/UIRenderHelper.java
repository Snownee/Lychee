package snownee.lychee.client.gui;

import org.joml.Matrix4f;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import snownee.kiwi.util.Color;

public class UIRenderHelper {

	/**
	 * An FBO that has a stencil buffer for use wherever stencil are necessary. Forcing the main FBO to have a stencil
	 * buffer will cause GL error spam when using fabulous graphics.
	 */
	public static CustomRenderTarget framebuffer;

	public static void init() {
		RenderSystem.recordRenderCall(() -> {
			Window mainWindow = Minecraft.getInstance().getWindow();
			framebuffer = CustomRenderTarget.create(mainWindow);
		});
	}

	public static void drawColoredTexture(
			GuiGraphics graphics,
			Color c,
			int left,
			int top,
			int z,
			float tex_left,
			float tex_top,
			int tex_width,
			int tex_height,
			int sheet_width,
			int sheet_height
	) {
		drawColoredTexture(
				graphics,
				c,
				left,
				left + tex_width,
				top,
				top + tex_height,
				z,
				tex_width,
				tex_height,
				tex_left,
				tex_top,
				sheet_width,
				sheet_height
		);
	}

	private static void drawColoredTexture(
			GuiGraphics graphics,
			Color c,
			int left,
			int right,
			int top,
			int bot,
			int z,
			int tex_width,
			int tex_height,
			float tex_left,
			float tex_top,
			int sheet_width,
			int sheet_height
	) {
		drawTexturedQuad(
				graphics.pose().last().pose(),
				c,
				left,
				right,
				top,
				bot,
				z,
				(tex_left + 0.0F) / (float) sheet_width,
				(tex_left + (float) tex_width) / (float) sheet_width,
				(tex_top + 0.0F) / (float) sheet_height,
				(tex_top + (float) tex_height) / (float) sheet_height
		);
	}

	private static void drawTexturedQuad(
			Matrix4f m,
			Color c,
			int left,
			int right,
			int top,
			int bot,
			int z,
			float u1,
			float u2,
			float v1,
			float v2
	) {
		Tesselator tesselator = Tesselator.getInstance();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferbuilder.addVertex(m, (float) left, (float) bot, (float) z).setColor(
				c.getRed(),
				c.getGreen(),
				c.getBlue(),
				c.getAlpha()
		).setUv(u1, v2);
		bufferbuilder.addVertex(m, (float) right, (float) bot, (float) z).setColor(
				c.getRed(),
				c.getGreen(),
				c.getBlue(),
				c.getAlpha()
		).setUv(u2, v2);
		bufferbuilder.addVertex(m, (float) right, (float) top, (float) z).setColor(
				c.getRed(),
				c.getGreen(),
				c.getBlue(),
				c.getAlpha()
		).setUv(u2, v1);
		bufferbuilder.addVertex(m, (float) left, (float) top, (float) z).setColor(
				c.getRed(),
				c.getGreen(),
				c.getBlue(),
				c.getAlpha()
		).setUv(u1, v1);
		tesselator.clear();
		RenderSystem.disableBlend();
	}

	public static void flipForGuiRender(PoseStack poseStack) {
		poseStack.mulPose(new Matrix4f().scaling(1, -1, 1));
	}

	public static class CustomRenderTarget extends RenderTarget {

		public CustomRenderTarget(boolean useDepth) {
			super(useDepth);
		}

		public static CustomRenderTarget create(Window mainWindow) {
			CustomRenderTarget framebuffer = new CustomRenderTarget(true);
			framebuffer.resize(mainWindow.getWidth(), mainWindow.getHeight(), Minecraft.ON_OSX);
			framebuffer.setClearColor(0, 0, 0, 0);
			// framebuffer.enableStencil(); // necessary?
			return framebuffer;
		}

		public void renderWithAlpha(float alpha) {
			Window window = Minecraft.getInstance().getWindow();

			float vx = (float) window.getGuiScaledWidth();
			float vy = (float) window.getGuiScaledHeight();
			float tx = (float) viewWidth / (float) width;
			float ty = (float) viewHeight / (float) height;

			RenderSystem.enableDepthTest();
			RenderSystem.setShader(() -> Minecraft.getInstance().gameRenderer.blitShader);
			RenderSystem.getShader().setSampler("DiffuseSampler", colorTextureId);

			bindRead();

			Tesselator tessellator = Tesselator.getInstance();
			BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

			bufferbuilder.addVertex(0, vy, 0).setColor(1, 1, 1, alpha).setUv(0, 0);
			bufferbuilder.addVertex(vx, vy, 0).setColor(1, 1, 1, alpha).setUv(tx, 0);
			bufferbuilder.addVertex(vx, 0, 0).setColor(1, 1, 1, alpha).setUv(tx, ty);
			bufferbuilder.addVertex(0, 0, 0).setColor(1, 1, 1, alpha).setUv(0, ty);

			tessellator.clear();
			unbindRead();
		}

	}

}
