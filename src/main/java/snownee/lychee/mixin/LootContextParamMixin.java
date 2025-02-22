package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import snownee.kiwi.util.KUtil;
import snownee.lychee.LycheeLootContextParams;

@Mixin(LootContextParam.class)
public class LootContextParamMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void lychee_init(ResourceLocation name, CallbackInfo ci) {
		LycheeLootContextParams.ALL.put(KUtil.trimRL(name.toString()), (LootContextParam<?>) (Object) this);
	}
}
