package snownee.lychee.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import snownee.lychee.util.render.EmptyVirtualBlockGetter;

@Mixin(BlockRenderDispatcher.class)
public abstract class BlockRenderDispatcherMixin {
	@Shadow
	public abstract void renderLiquid(
			BlockPos pos,
			BlockAndTintGetter level,
			VertexConsumer builder,
			BlockState blockState,
			FluidState fluidState);

	@WrapOperation(
			method = "renderSingleBlock", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/block/ModelBlockRenderer;renderModel(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/renderer/block/model/BlockStateModel;FFFII)V"))
	private void renderSingleBlock(
			PoseStack.Pose pose,
			VertexConsumer builder,
			BlockStateModel model,
			float r,
			float g,
			float b,
			int lightCoords,
			int overlayCoords,
			Operation<Void> original,
			@Local(argsOnly = true) BlockState state,
			@Local(argsOnly = true) MultiBufferSource bufferSource) {
		original.call(pose, builder, model, r, g, b, lightCoords, overlayCoords);
		if (state.getFluidState().isEmpty()) {
			return;
		}
		FluidState fluidState = state.getFluidState();
		VertexConsumer buffer = bufferSource.getBuffer(RenderTypes.translucentMovingBlock());
		renderLiquid(BlockPos.ZERO, EmptyVirtualBlockGetter.FULL_BRIGHT, buffer, fluidState.createLegacyBlock(), fluidState);
	}
}
