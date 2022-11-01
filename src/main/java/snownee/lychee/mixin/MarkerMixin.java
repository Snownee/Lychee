package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Marker;
import snownee.lychee.core.ActionRuntime.State;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.post.Delay.LycheeMarker;
import snownee.lychee.core.recipe.LycheeRecipe;

@Mixin(Marker.class)
public class MarkerMixin implements LycheeMarker {

	private int lychee$ticks;
	private LycheeRecipe<?> lychee$recipe;
	private LycheeContext lychee$ctx;

	@Override
	public void lychee$setContext(LycheeRecipe<?> recipe, LycheeContext ctx) {
		lychee$ctx = ctx;
		lychee$recipe = recipe;
	}

	@Override
	public LycheeContext lychee$getContext() {
		return lychee$ctx;
	}

	@Override
	public void lychee$addDelay(int delay) {
		lychee$ticks += delay;
	}

	@Inject(at = @At("HEAD"), method = "tick")
	private void lychee_tick(CallbackInfo ci) {
		if (lychee$recipe == null || lychee$ctx == null) {
			return;
		}
		if (lychee$ticks-- > 0) {
			return;
		}
		lychee$ctx.runtime.state = State.RUNNING;
		lychee$ctx.runtime.run(lychee$recipe, lychee$ctx);
		if (lychee$ctx.runtime.state == State.STOPPED) {
			getEntity().discard();
		}
	}

}
