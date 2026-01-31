package snownee.lychee.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.Lighting;

import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.gui.render.state.pip.GuiEntityRenderState;
import snownee.lychee.client.gui.GuiGameElement;

@Mixin(GuiEntityRenderer.class)
public abstract class GuiEntityRendererMixin {
	@WrapOperation(
			method = "renderToTexture*",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/platform/Lighting;setupFor(Lcom/mojang/blaze3d/platform/Lighting$Entry;)V"))
	private void renderToTexture(
			Lighting lighting,
			Lighting.Entry entry,
			Operation<Void> original,
			@Local(argsOnly = true) GuiEntityRenderState entityState) {
		original.call(lighting, entityState.renderState().getDataOrDefault(GuiGameElement.CUSTOM_LIGHTING, entry));
	}
}
