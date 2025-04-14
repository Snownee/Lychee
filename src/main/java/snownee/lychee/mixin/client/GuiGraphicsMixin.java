package snownee.lychee.mixin.client;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.client.gui.LycheeGuiGraphics;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin implements LycheeGuiGraphics {

	@Shadow
	@Final
	private PoseStack pose;
	@Unique
	private @Nullable Function<ResourceLocation, RenderType> lychee$renderType;

	@Shadow
	public abstract MultiBufferSource.BufferSource bufferSource();

	@Inject(method = "innerBlit(Lnet/minecraft/resources/ResourceLocation;IIIIIFFFF)V", at = @At("HEAD"), cancellable = true)
	private void lychee_innerBlit(
			ResourceLocation atlasLocation,
			int x1,
			int x2,
			int y1,
			int y2,
			int blitOffset,
			float minU,
			float maxU,
			float minV,
			float maxV,
			CallbackInfo ci) {
		if (lychee$renderType == null) {
			return;
		}
		MultiBufferSource.BufferSource bufferSource = bufferSource();
		RenderType renderType = lychee$renderType.apply(atlasLocation);
		VertexConsumer buffer = bufferSource.getBuffer(renderType);
		Matrix4f matrix4f = pose.last().pose();
//		bufferBuilder.addVertex(matrix4f, (float) x1, (float) y1, (float) blitOffset).setUv(minU, minV);
//		bufferBuilder.addVertex(matrix4f, (float) x1, (float) y2, (float) blitOffset).setUv(minU, maxV);
//		bufferBuilder.addVertex(matrix4f, (float) x2, (float) y2, (float) blitOffset).setUv(maxU, maxV);
//		bufferBuilder.addVertex(matrix4f, (float) x2, (float) y1, (float) blitOffset).setUv(maxU, minV);
		buffer.addVertex(matrix4f, x1, y1, blitOffset).setUv(minU, minV);
		buffer.addVertex(matrix4f, x1, y2, blitOffset).setUv(minU, maxV);
		buffer.addVertex(matrix4f, x2, y2, blitOffset).setUv(maxU, maxV);
		buffer.addVertex(matrix4f, x2, y1, blitOffset).setUv(maxU, minV);
		bufferSource.endBatch(renderType);
		ci.cancel();
	}

	@Override
	public void lychee$setRenderType(@Nullable Function<ResourceLocation, RenderType> renderType) {
		this.lychee$renderType = renderType;
	}
}
