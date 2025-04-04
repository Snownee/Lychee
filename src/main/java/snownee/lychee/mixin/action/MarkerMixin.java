package snownee.lychee.mixin.action;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeLootContextParamSets;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.action.ActionData;
import snownee.lychee.util.action.ActionMarker;
import snownee.lychee.util.context.LycheeContextKey;

@Mixin(Marker.class)
public class MarkerMixin implements ActionMarker {
	@Unique
	@Nullable
	private ActionData lychee$data;

	@Override
	public ActionData lychee$getData() {
		return lychee$data;
	}

	@Override
	public void lychee$setData(final ActionData lychee$data) {
		this.lychee$data = lychee$data;
	}

	@Inject(at = @At("HEAD"), method = "tick")
	private void lychee_tick(CallbackInfo ci) {
		if (lychee$data == null || lychee$data.getContext().isEmpty()) {
			return;
		}
		if (lychee$data.consumeDelayedTicks() > 0) {
			return;
		}

		var context = lychee$data.getContext().get();
		final var actionContext = context.get(LycheeContextKey.ACTION);
		actionContext.state = ActionContext.State.RUNNING;
		actionContext.run(context);
		if (actionContext.state == ActionContext.State.STOPPED) {
			lychee$self().discard();
		}
	}

	@Inject(at = @At("HEAD"), method = "readAdditionalSaveData")
	private void lychee_readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
		if (!compoundTag.contains("lychee:action")) {
			return;
		}
		final var tag = compoundTag.getCompound("lychee:action");
		lychee$data = ActionData.CODEC
				.parse(NbtOps.INSTANCE, tag)
				.getOrThrow((err) -> {
					Lychee.LOGGER.debug("Load Lychee action data from marker failed from tag: " + tag);
					lychee$self().discard();
					return new IllegalStateException("Load Lychee action data from marker failed with error: " + err);
				});
		if (lychee$data.getContext().isEmpty()) {
			lychee$self().discard();
		}
		var context = lychee$data.getContext().get();
		context.put(LycheeContextKey.LEVEL, lychee$self().level());
		var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		lootParamsContext.setParam(LootContextParams.ORIGIN, lychee$self().position());
		lootParamsContext.validate(LycheeLootContextParamSets.ALL);
	}

	@Inject(at = @At("HEAD"), method = "addAdditionalSaveData")
	private void lychee_addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
		if (lychee$data == null || lychee$data.getContext().isEmpty()) {
			return;
		}

		compoundTag.put(
				"lychee:action",
				ActionData.CODEC.encodeStart(NbtOps.INSTANCE, lychee$data).getOrThrow((err) -> {
					Lychee.LOGGER.debug("Save Lychee action data to marker failed from data: {}", lychee$data);
					return new IllegalStateException("Save Lychee action data to marker failed with error: " + err);
				})
		);
	}
}
