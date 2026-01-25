package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import snownee.lychee.LycheeContextKeys;

@Mixin(ContextKey.class)
public class LootContextParamMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void lychee_init(Identifier name, CallbackInfo ci) {
		LycheeContextKeys.ALL.put(name, (ContextKey<?>) (Object) this);
	}
}
