package snownee.lychee.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.Lighting;
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;

import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import net.minecraft.util.Unit;
import snownee.lychee.client.gui.GuiGameElement;

@Mixin(GuiEntityRenderer.class)
public abstract class GuiEntityRendererMixin {
	@WrapOperation(
			method = "renderToTexture*",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/platform/Lighting;setupFor(Lcom/mojang/blaze3d/platform/Lighting$Entry;)V"))
	private void lychee_setCustomLighting(
			Lighting lighting,
			Lighting.Entry entry,
			Operation<Void> original,
			@Local(argsOnly = true) GuiEntityRenderState entityState) {
		original.call(lighting, ((FabricRenderState) entityState.renderState()).getDataOrDefault(GuiGameElement.CUSTOM_LIGHTING, entry));
	}

	@WrapOperation(
			method = "renderToTexture*", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderAllFeatures()V"))
	private void lychee_setRenderFluidFlag(
			FeatureRenderDispatcher featureRenderDispatcher,
			Operation<Void> original,
			@Local(argsOnly = true) GuiEntityRenderState entityState) {
		boolean drawFluid = ((FabricRenderState) entityState.renderState()).getData(GuiGameElement.DRAW_FLUID_STATE) != null;
		if (drawFluid) {
			GuiGameElement.DRAW_FLUID_STATE_FLAG.set(Unit.INSTANCE);
			original.call(featureRenderDispatcher);
			GuiGameElement.DRAW_FLUID_STATE_FLAG.remove();
		} else {
			original.call(featureRenderDispatcher);
		}
	}
}
