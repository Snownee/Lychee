package snownee.lychee.client.gui;

import org.joml.Vector3f;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRendering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;
import snownee.lychee.util.render.EmptyVirtualBlockGetter;

public class GuiFluidRenderer extends PictureInPictureRenderer<GuiFluidRenderState> {
	public GuiFluidRenderer(MultiBufferSource.BufferSource bufferSource) {
		super(bufferSource);
	}

	@Override
	public Class<GuiFluidRenderState> getRenderStateClass() {
		return GuiFluidRenderState.class;
	}

	@Override
	protected void renderToTexture(GuiFluidRenderState renderState, PoseStack poseStack) {
		FluidState fluidState = renderState.fluidState();
		FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(fluidState.getType());
		if (handler == null) {
			return;
		}
		Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
		Vector3f translation = renderState.translation();
		float scale = renderState.scale();
		poseStack.translate(translation.x, translation.y, translation.z);
		poseStack.mulPose(renderState.rotation());
		poseStack.scale(scale, scale, scale);
		VertexConsumer buffer = new InjectPose(bufferSource.getBuffer(Sheets.translucentBlockItemSheet()), poseStack);
//		VertexConsumer buffer = bufferSource.getBuffer(RenderTypes.translucentMovingBlock());
		FluidRendering.render(
				handler,
				EmptyVirtualBlockGetter.FULL_BRIGHT,
				BlockPos.ZERO,
				buffer,
				fluidState.createLegacyBlock(),
				fluidState,
				new FluidRendering.DefaultRenderer() {});
	}

	@Override
	protected String getTextureLabel() {
		return "lychee fluid";
	}

	public record InjectPose(VertexConsumer base, PoseStack pose) implements VertexConsumer {
		@Override
		public VertexConsumer addVertex(float x, float y, float z) {
//			Vector3f pos = pose.last().pose().transformPosition(x, y, z, new Vector3f());
//			Lychee.LOGGER.info("Transformed position: {}, {}, {}", pos.x(), pos.y(), pos.z());
			return base.addVertex(pose.last(), x, y, z);
		}

		@Override
		public VertexConsumer setColor(int r, int g, int b, int a) {
			return base.setColor(r, g, b, a);
		}

		@Override
		public VertexConsumer setColor(int color) {
			return base.setColor(color);
		}

		@Override
		public VertexConsumer setUv(float u, float v) {
			return base.setUv(u, v);
		}

		@Override
		public VertexConsumer setUv1(int u, int v) {
			return base.setUv1(u, v);
		}

		@Override
		public VertexConsumer setUv2(int u, int v) {
			return base.setUv2(u, v);
		}

		@Override
		public VertexConsumer setNormal(float x, float y, float z) {
			return base.setNormal(pose.last(), x, y, z);
		}

		@Override
		public VertexConsumer setLineWidth(float width) {
			return base.setLineWidth(width);
		}
	}
}
