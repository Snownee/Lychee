package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public interface GameRendererAccess {

	@Invoker
	double callGetFov(Camera pActiveRenderInfo, float pPartialTicks, boolean pUseFOVSetting);

}
