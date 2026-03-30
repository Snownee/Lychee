package snownee.lychee.mixin.client;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import snownee.kiwi.util.client.SmartKey;
import snownee.lychee.LycheeConfig;

@Mixin(GuiRenderState.class)
public class GuiRenderStateMixin {
	@WrapMethod(method = "addDebugRectangleIfEnabled")
	private void lychee_disableDebugRectangles(ScreenRectangle bounds, Operation<Void> original) {
		if (!LycheeConfig.debug || !SmartKey.hasControlDown()) {
			original.call(bounds);
		}
	}
}
