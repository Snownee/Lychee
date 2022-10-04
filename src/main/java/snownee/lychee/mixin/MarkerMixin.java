package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Marker;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.post.Delay.LycheeMarker;

@Mixin(Marker.class)
public class MarkerMixin implements LycheeMarker {

	private int lychee$ticks;
	private ResourceLocation lychee$recipeId;
	private LycheeContext lychee$ctx;

	@Override
	public void lychee$setContext(ResourceLocation recipeId, LycheeContext ctx) {
		lychee$ctx = ctx;
		lychee$recipeId = recipeId;
	}

	@Override
	public LycheeContext lychee$getContext() {
		return lychee$ctx;
	}

	@Inject(at = @At("HEAD"), method = "tick")
	private void lychee_tick() {
		if (lychee$recipeId == null) {
			return;
		}
	}

}
