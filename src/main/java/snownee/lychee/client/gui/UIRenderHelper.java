package snownee.lychee.client.gui;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
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
import snownee.lychee.util.Color;

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

	public static void updateWindowSize(Window mainWindow) {
		if (framebuffer != null) {
			framebuffer.resize(mainWindow.getWidth(), mainWindow.getHeight(), Minecraft.ON_OSX);
		}
	}

	public static void drawFramebuffer(float alpha) {
		framebuffer.renderWithAlpha(alpha);
	}

	/**
	 * Switch from src to dst, after copying the contents of src to dst.
	 */
	public static void swapAndBlitColor(RenderTarget src, RenderTarget dst) {
		GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, src.frameBufferId);
		GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, dst.frameBufferId);
		GlStateManager._glBlitFrameBuffer(
				0,
				0,
				src.viewWidth,
				src.viewHeight,
				0,
				0,
				dst.viewWidth,
				dst.viewHeight,
				GL30.GL_COLOR_BUFFER_BIT,
				GL20.GL_LINEAR
		);

		GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, dst.frameBufferId);
	}

	//just like AbstractGui#drawTexture, but with a color at every vertex
	public static void drawColoredTexture(
			GuiGraphics graphics,
			Color c,
			int x,
			int y,
			int tex_left,
			int tex_top,
			int width,
			int height
	) {
		drawColoredTexture(graphics, c, x, y, 0, (float) tex_left, (float) tex_top, width, height, 256, 256);
	}

	public static void drawColoredTexture(
			GuiGraphics graphics,
			Color c,
			int x,
			int y,
			int z,
			float tex_left,
			float tex_top,
			int width,
			int height,
			int sheet_width,
			int sheet_height
	) {
		drawColoredTexture(
				graphics,
				c,
				x,
				x + width,
				y,
				y + height,
				z,
				width,
				height,
				tex_left,
				tex_top,
				sheet_width,
				sheet_height
		);
	}

	public static void drawStretched(
			GuiGraphics graphics, int left, int top, int w, int h, int z,
			AllGuiTextures tex) {
		tex.bind();
		drawTexturedQuad(
				graphics.pose().last().pose(),
				Color.WHITE,
				left,
				left + w,
				top,
				top + h,
				z,
				tex.startX / 256f,
				(tex.startX + tex.width) / 256f,
				tex.startY / 256f,
				(tex.startY + tex.height) / 256f
		);
	}

	public static void drawCropped(GuiGraphics graphics, int left, int top, int w, int h, int z, AllGuiTextures tex) {
		tex.bind();
		drawTexturedQuad(
				graphics.pose().last().pose(),
				Color.WHITE,
				left,
				left + w,
				top,
				top + h,
				z,
				tex.startX / 256f,
				(tex.startX + w) / 256f,
				tex.startY / 256f,
				(tex.startY + h) / 256f
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
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
		bufferbuilder.vertex(m, (float) left, (float) bot, (float) z).color(
				c.getRed(),
				c.getGreen(),
				c.getBlue(),
				c.getAlpha()
		).uv(u1, v2).endVertex();
		bufferbuilder.vertex(m, (float) right, (float) bot, (float) z).color(
				c.getRed(),
				c.getGreen(),
				c.getBlue(),
				c.getAlpha()
		).uv(u2, v2).endVertex();
		bufferbuilder.vertex(m, (float) right, (float) top, (float) z).color(
				c.getRed(),
				c.getGreen(),
				c.getBlue(),
				c.getAlpha()
		).uv(u2, v1).endVertex();
		bufferbuilder.vertex(m, (float) left, (float) top, (float) z).color(
				c.getRed(),
				c.getGreen(),
				c.getBlue(),
				c.getAlpha()
		).uv(u1, v1).endVertex();
		tesselator.end();
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
			BufferBuilder bufferbuilder = tessellator.getBuilder();
			bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);

			bufferbuilder.vertex(0, vy, 0).color(1, 1, 1, alpha).uv(0, 0).endVertex();
			bufferbuilder.vertex(vx, vy, 0).color(1, 1, 1, alpha).uv(tx, 0).endVertex();
			bufferbuilder.vertex(vx, 0, 0).color(1, 1, 1, alpha).uv(tx, ty).endVertex();
			bufferbuilder.vertex(0, 0, 0).color(1, 1, 1, alpha).uv(0, ty).endVertex();

			tessellator.end();
			unbindRead();
		}

	}

}
