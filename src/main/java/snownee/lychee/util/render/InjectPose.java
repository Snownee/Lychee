package snownee.lychee.util.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public record InjectPose(VertexConsumer base, PoseStack.Pose pose) implements VertexConsumer {
	@Override
	public VertexConsumer addVertex(float x, float y, float z) {
		return base.addVertex(pose, x, y, z);
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
		return base.setNormal(pose, x, y, z);
	}

	@Override
	public VertexConsumer setLineWidth(float width) {
		return base.setLineWidth(width);
	}
}
