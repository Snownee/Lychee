package snownee.lychee.mixin.action;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.serialization.DataResult;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
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
	@Nullable
	public ActionData lychee$getData() {
		return lychee$data;
	}

	@Override
	public void lychee$setData(final ActionData lychee$data) {
		this.lychee$data = lychee$data;
	}

	@Inject(at = @At("HEAD"), method = "tick")
	private void lychee_tick(CallbackInfo ci) {
		if (lychee$data == null) {
			return;
		}
		if (lychee$self().tickCount > 5 * 60 * 20) {
			lychee$self().discard();
			return;
		}
		if (lychee$data.consumeDelayedTicks() > 0) {
			return;
		}

		var context = lychee$data.getContext();
		// When `readAdditionalSaveData` is called, the level can be null
		context.put(LycheeContextKey.LEVEL, lychee$self().level());
		final var actionContext = context.get(LycheeContextKey.ACTION);
		actionContext.state = ActionContext.State.RUNNING;
		actionContext.run(context);
		if (actionContext.state == ActionContext.State.STOPPED) {
			lychee$self().discard();
		}
	}

	@Inject(at = @At("HEAD"), method = "readAdditionalSaveData")
	private void lychee_readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
		if (!compoundTag.contains("lychee")) {
			return;
		}
		final var tag = compoundTag.getCompound("lychee");
		DataResult<ActionData> result = ActionData.CODEC.parse(NbtOps.INSTANCE, tag);
		if (result.isError()) {
			Lychee.LOGGER.error("Load Lychee action data: {} -> {}", tag, result.error().orElseThrow().message());
			lychee$self().discard();
			return;
		}
		lychee$data = result.getOrThrow();
		var context = lychee$data.getContext();
		if (context.has(LycheeContextKey.RECIPE_ID, false)) {
			context.has(LycheeContextKey.RECIPE, true);
		}
		context.put(LycheeContextKey.MARKER, this);
		var lootParams = context.get(LycheeContextKey.LOOT_PARAMS);
		lootParams.setParam(LootContextParams.ORIGIN, lychee$self().position());
		try {
			lootParams.validate(LycheeLootContextParamSets.ALL);
		} catch (IllegalArgumentException e) {
			Lychee.LOGGER.error("Load Lychee action data: {} -> {}", tag, e.getMessage());
			lychee$self().discard();
		}
	}

	@Inject(at = @At("HEAD"), method = "addAdditionalSaveData")
	private void lychee_addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
		if (lychee$data == null) {
			return;
		}

		DataResult<Tag> result = ActionData.CODEC.encodeStart(NbtOps.INSTANCE, lychee$data);
		if (result.isSuccess()) {
			compoundTag.put("lychee", result.getOrThrow());
		} else {
			Lychee.LOGGER.error("{}: {}", lychee$data, result.error().orElseThrow().message());
		}
	}
}
