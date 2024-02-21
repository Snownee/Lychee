package snownee.lychee.mixin.action;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.Marker;
import snownee.lychee.Lychee;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.action.ActionData;
import snownee.lychee.util.action.ActionMarker;
import snownee.lychee.util.context.LycheeContextKey;

@Mixin(Marker.class)
public class MarkerMixin implements ActionMarker {
	@Unique
	private ActionData lychee$data;

	public ActionData lychee$getData() {
		return lychee$data;
	}

	public void lychee$setData(final ActionData lychee$data) {
		this.lychee$data = lychee$data;
	}

	@Inject(at = @At("HEAD"), method = "tick")
	private void lychee_tick(CallbackInfo ci) {
		if (lychee$data.getContext() == null) {
			return;
		}
		if (lychee$data.consumeDelayedTicks() > 0) {
			return;
		}

		final var actionContext = lychee$data.getContext().get(LycheeContextKey.ACTION);
		actionContext.state = ActionContext.State.RUNNING;
		actionContext.run(lychee$data.getRecipe(), lychee$data.getContext());
		if (actionContext.state == ActionContext.State.STOPPED) {
			self().discard();
		}
	}

	@Inject(at = @At("HEAD"), method = "readAdditionalSaveData")
	private void lychee_readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
		if (compoundTag.contains("lychee:action")) {
			final var tag = compoundTag.getCompound("lychee:action");
			lychee$data = ActionData.CODEC
					.parse(NbtOps.INSTANCE, tag)
					.getOrThrow(false, (err) -> {
						Lychee.LOGGER.error("Load Lychee action data from marker failed with error:" + err);
						Lychee.LOGGER.debug("Load Lychee action data from marker failed from tag:" + tag);
						self().discard();
					});
		}
		if (lychee$data.getContext() == null
				&& self().hasCustomName()
				&& Lychee.ID.equals(self().getCustomName().getString())) {
			self().discard();
		}
	}

	@Inject(at = @At("HEAD"), method = "addAdditionalSaveData")
	private void lychee_addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
		if (lychee$data.getContext() == null) {
			return;
		}

		compoundTag.put(
				"lychee:action",
				ActionData.CODEC.encodeStart(NbtOps.INSTANCE, lychee$data).getOrThrow(false, (err) -> {
					Lychee.LOGGER.error("Save Lychee action data to marker failed with error:" + err);
					Lychee.LOGGER.debug("Save Lychee action data to marker failed from data:" + lychee$data);
				})
		);
	}
}
