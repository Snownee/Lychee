package snownee.lychee.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import snownee.lychee.lightning_channeling.LightningChannelingRecipe;

@Mixin(LightningBolt.class)
public class LightningBoltMixin {

	@Inject(
			at = @At(
					value = "INVOKE", target = "Ljava/util/Set;addAll(Ljava/util/Collection;)Z"
			), method = "tick", locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void lychee_tick(CallbackInfo ci, List<Entity> list1) {
		LightningChannelingRecipe.on((LightningBolt) (Object) this, list1);
	}

}
