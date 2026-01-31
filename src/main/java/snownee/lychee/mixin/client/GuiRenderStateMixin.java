package snownee.lychee.mixin.client;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.gui.render.state.GuiRenderState;
import snownee.kiwi.util.client.SmartKey;
import snownee.lychee.LycheeConfig;

@Mixin(GuiRenderState.class)
public class GuiRenderStateMixin {
	@WrapOperation(
			method = "sumbitDebugRectangleIfEnabled",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/SharedConstants;DEBUG_RENDER_UI_LAYERING_RECTANGLES:Z",
					opcode = Opcodes.GETSTATIC))
	private static boolean lychee_disableDebugRectangles(Operation<Boolean> original) {
		if (LycheeConfig.debug && SmartKey.hasControlDown()) {
			return true;
		}
		return original.call();
	}
}
