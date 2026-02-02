package snownee.lychee.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.util.render.EmptyVirtualBlockGetter;
import snownee.lychee.util.render.InjectPose;

@Mixin(BlockRenderDispatcher.class)
public abstract class BlockRenderDispatcherMixin {
	@Shadow
	public abstract void renderLiquid(
			BlockPos pos,
			BlockAndTintGetter level,
			VertexConsumer builder,
			BlockState blockState,
			FluidState fluidState);

	@Inject(method = "renderSingleBlock", at = @At("HEAD"))
	private void lychee_renderSingleBlock(
			BlockState state,
			PoseStack poseStack,
			MultiBufferSource bufferSource,
			int lightCoords,
			int overlayCoords,
			CallbackInfo ci) {
		if (state.getFluidState().isEmpty() || GuiGameElement.DRAW_FLUID_STATE_FLAG.get() == null) {
			return;
		}
		FluidState fluidState = state.getFluidState();
		VertexConsumer buffer = bufferSource.getBuffer(RenderTypes.translucentMovingBlock());
		renderLiquid(
				BlockPos.ZERO,
				EmptyVirtualBlockGetter.FULL_DARK,
				new InjectPose(buffer, poseStack.last()),
				fluidState.createLegacyBlock(),
				fluidState);
	}
}
