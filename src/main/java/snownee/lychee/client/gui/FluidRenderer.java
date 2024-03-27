package snownee.lychee.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FluidState;

public class FluidRenderer {

	public static VertexConsumer getFluidBuilder(MultiBufferSource buffer) {
		return buffer.getBuffer(RenderTypes.getFluid());
	}

	//	public static void renderFluidStream(FluidStack fluidStack, Direction direction, float radius, float progress,
	//		boolean inbound, MultiBufferSource buffer, PoseStack ms, int light) {
	//		renderFluidStream(fluidStack, direction, radius, progress, inbound, getFluidBuilder(buffer), ms, light);
	//	}
	//
	//	public static void renderFluidStream(FluidStack fluidStack, Direction direction, float radius, float progress,
	//		boolean inbound, VertexConsumer builder, PoseStack ms, int light) {
	//		Fluid fluid = fluidStack.getFluid();
	//		FluidAttributes fluidAttributes = fluid.getAttributes();
	//		Function<ResourceLocation, TextureAtlasSprite> spriteAtlas = Minecraft.getInstance()
	//			.getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
	//		TextureAtlasSprite flowTexture = spriteAtlas.apply(fluidAttributes.getFlowingTexture(fluidStack));
	//		TextureAtlasSprite stillTexture = spriteAtlas.apply(fluidAttributes.getStillTexture(fluidStack));
	//
	//		int color = fluidAttributes.getColor(fluidStack);
	//		int blockLightIn = (light >> 4) & 0xF;
	//		int luminosity = Math.max(blockLightIn, fluidAttributes.getLuminosity(fluidStack));
	//		light = (light & 0xF00000) | luminosity << 4;
	//
	//		if (inbound)
	//			direction = direction.getOpposite();
	//
	//		TransformStack msr = TransformStack.cast(ms);
	//		ms.pushPose();
	//		msr.centre()
	//			.rotateY(AngleHelper.horizontalAngle(direction))
	//			.rotateX(direction == Direction.UP ? 180 : direction == Direction.DOWN ? 0 : 270)
	//			.unCentre();
	//		ms.translate(.5, 0, .5);
	//
	//		float h = radius;
	//		float hMin = -radius;
	//		float hMax = radius;
	//		float y = inbound ? 1 : .5f;
	//		float yMin = y - Mth.clamp(progress * .5f, 0, 1);
	//		float yMax = y;
	//
	//		for (int i = 0; i < 4; i++) {
	//			ms.pushPose();
	//			renderFlowingTiledFace(Direction.SOUTH, hMin, yMin, hMax, yMax, h,
	//				builder, ms, light, color, flowTexture);
	//			ms.popPose();
	//			msr.rotateY(90);
	//		}
	//
	//		if (progress != 1)
	//			renderStillTiledFace(Direction.DOWN, hMin, hMin, hMax, hMax, yMin,
	//				builder, ms, light, color, stillTexture);
	//
	//		ms.popPose();
	//	}

	public static void renderFluidBox(
			FluidState fluidState,
			float xMin,
			float yMin,
			float zMin,
			float xMax,
			float yMax,
			float zMax,
			MultiBufferSource buffer,
			PoseStack ms,
			int light,
			boolean renderBottom
	) {
		renderFluidBox(
				fluidState,
				xMin,
				yMin,
				zMin,
				xMax,
				yMax,
				zMax,
				getFluidBuilder(buffer),
				ms,
				light,
				renderBottom
		);
	}

	public static void renderFluidBox(
			FluidState fluidState,
			float xMin,
			float yMin,
			float zMin,
			float xMax,
			float yMax,
			float zMax,
			VertexConsumer builder,
			PoseStack ms,
			int light,
			boolean renderBottom
	) {
		var fluid = fluidState.getType();
		var fluidTexture = FluidVariantRendering.getSprites(FluidVariant.of(fluid))[0];

		var color = FluidRenderHandlerRegistry.INSTANCE.get(fluid).getFluidColor(null, null, fluidState) | 0xFF000000;
		//		int blockLightIn = (light >> 4) & 0xF;
		//		int luminosity = Math.max(blockLightIn, fluidAttributes.getLuminosity(fluidStack));
		//		light = (light & 0xF00000) | luminosity << 4;

		//		Vec3 center = new Vec3(xMin + (xMax - xMin) / 2, yMin + (yMax - yMin) / 2, zMin + (zMax - zMin) / 2);
		ms.pushPose();
		//FIXME
		//		if (fluidStack.getFluid().getAttributes().isLighterThanAir())
		//			TransformStack.cast(ms).translate(center).rotateX(180).translateBack(center);

		//		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
		//		RenderSystem.setShaderColor(1, 1, 1, 1);
		//		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		for (var side : Direction.values()) {
			if (side == Direction.DOWN && !renderBottom) {
				continue;
			}

			var positive = side.getAxisDirection() == AxisDirection.POSITIVE;
			if (side.getAxis().isHorizontal()) {
				if (side.getAxis() == Axis.X) {
					renderStillTiledFace(
							side,
							zMin,
							yMin,
							zMax,
							yMax,
							positive ? xMax : xMin,
							builder,
							ms,
							light,
							color,
							fluidTexture
					);
				} else {
					renderStillTiledFace(
							side,
							xMin,
							yMin,
							xMax,
							yMax,
							positive ? zMax : zMin,
							builder,
							ms,
							light,
							color,
							fluidTexture
					);
				}
			} else {
				renderStillTiledFace(
						side,
						xMin,
						zMin,
						xMax,
						zMax,
						positive ? yMax : yMin,
						builder,
						ms,
						light,
						color,
						fluidTexture
				);
			}
		}

		ms.popPose();
	}

	public static void renderStillTiledFace(
			Direction dir,
			float left,
			float down,
			float right,
			float up,
			float depth,
			VertexConsumer builder,
			PoseStack ms,
			int light,
			int color,
			TextureAtlasSprite texture
	) {
		FluidRenderer.renderTiledFace(dir, left, down, right, up, depth, builder, ms, light, color, texture, 1);
	}

	public static void renderFlowingTiledFace(
			Direction dir,
			float left,
			float down,
			float right,
			float up,
			float depth,
			VertexConsumer builder,
			PoseStack ms,
			int light,
			int color,
			TextureAtlasSprite texture
	) {
		FluidRenderer.renderTiledFace(dir, left, down, right, up, depth, builder, ms, light, color, texture, 0.5f);
	}

	public static void renderTiledFace(
			Direction dir,
			float left,
			float down,
			float right,
			float up,
			float depth,
			VertexConsumer builder,
			PoseStack ms,
			int light,
			int color,
			TextureAtlasSprite texture,
			float textureScale
	) {
		var positive = dir.getAxisDirection() == Direction.AxisDirection.POSITIVE;
		var horizontal = dir.getAxis().isHorizontal();
		var x = dir.getAxis() == Axis.X;

		var shrink = texture.uvShrinkRatio() * 0.25f * textureScale;
		var centerU = texture.getU0() + (texture.getU1() - texture.getU0()) * 0.5f * textureScale;
		var centerV = texture.getV0() + (texture.getV1() - texture.getV0()) * 0.5f * textureScale;

		float f;
		float x2 = 0;
		float y2 = 0;
		float u1, u2;
		float v1, v2;
		for (var x1 = left; x1 < right; x1 = x2) {
			f = Mth.floor(x1);
			x2 = Math.min(f + 1, right);
			if (dir == Direction.NORTH || dir == Direction.EAST) {
				f = Mth.ceil(x2);
				u1 = texture.getU((f - x2) * textureScale);
				u2 = texture.getU((f - x1) * textureScale);
			} else {
				u1 = texture.getU((x1 - f) * textureScale);
				u2 = texture.getU((x2 - f) * textureScale);
			}
			u1 = Mth.lerp(shrink, u1, centerU);
			u2 = Mth.lerp(shrink, u2, centerU);
			for (var y1 = down; y1 < up; y1 = y2) {
				f = Mth.floor(y1);
				y2 = Math.min(f + 1, up);
				if (dir == Direction.UP) {
					v1 = texture.getV((y1 - f) * textureScale);
					v2 = texture.getV((y2 - f) * textureScale);
				} else {
					f = Mth.ceil(y2);
					v1 = texture.getV((f - y2) * textureScale);
					v2 = texture.getV((f - y1) * textureScale);
				}
				v1 = Mth.lerp(shrink, v1, centerV);
				v2 = Mth.lerp(shrink, v2, centerV);

				if (horizontal) {
					if (x) {
						putVertex(builder, ms, depth, y2, positive ? x2 : x1, color, u1, v1, dir, light);
						putVertex(builder, ms, depth, y1, positive ? x2 : x1, color, u1, v2, dir, light);
						putVertex(builder, ms, depth, y1, positive ? x1 : x2, color, u2, v2, dir, light);
						putVertex(builder, ms, depth, y2, positive ? x1 : x2, color, u2, v1, dir, light);
					} else {
						putVertex(builder, ms, positive ? x1 : x2, y2, depth, color, u1, v1, dir, light);
						putVertex(builder, ms, positive ? x1 : x2, y1, depth, color, u1, v2, dir, light);
						putVertex(builder, ms, positive ? x2 : x1, y1, depth, color, u2, v2, dir, light);
						putVertex(builder, ms, positive ? x2 : x1, y2, depth, color, u2, v1, dir, light);
					}
				} else {
					putVertex(builder, ms, x1, depth, positive ? y1 : y2, color, u1, v1, dir, light);
					putVertex(builder, ms, x1, depth, positive ? y2 : y1, color, u1, v2, dir, light);
					putVertex(builder, ms, x2, depth, positive ? y2 : y1, color, u2, v2, dir, light);
					putVertex(builder, ms, x2, depth, positive ? y1 : y2, color, u2, v1, dir, light);
				}
			}
		}
	}

	private static void putVertex(
			VertexConsumer builder,
			PoseStack ms,
			float x,
			float y,
			float z,
			int color,
			float u,
			float v,
			Direction face,
			int light
	) {

		var normal = face.getNormal();
		var peek = ms.last();
		var a = color >> 24 & 0xff;
		var r = color >> 16 & 0xff;
		var g = color >> 8 & 0xff;
		var b = color & 0xff;

		builder.vertex(peek.pose(), x, y, z)
				.color(r, g, b, a)
				.uv(u, v)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(light)
				.normal(peek, normal.getX(), normal.getY(), normal.getZ())
				.endVertex();
	}

}
