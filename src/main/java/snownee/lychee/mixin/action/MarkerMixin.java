package snownee.lychee.mixin.action;

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.Lychee;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.action.ActionData;
import snownee.lychee.util.action.ActionMarker;
import snownee.lychee.util.context.LycheeContextKey;

@Mixin(Marker.class)
public abstract class MarkerMixin extends Entity implements ActionMarker {
	@Unique
	@Nullable
	private ActionData lychee$data;

	public MarkerMixin(EntityType<?> type, Level level) {
		super(type, level);
	}

	@Override
	@Nullable
	public ActionData lychee$getData() {
		return lychee$data;
	}

	@Override
	public void lychee$setData(final ActionData data) {
		this.lychee$data = data;
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

		var context = lychee$data.context();
		var actionContext = lychee$data.actionContext();
		// When `readAdditionalSaveData` is called, the level can be null
		context.put(LycheeContextKey.LEVEL, lychee$self().level());
		actionContext.state = ActionContext.State.RUNNING;
		actionContext.run(context);
		if (actionContext.state == ActionContext.State.STOPPED) {
			lychee$self().discard();
		}
	}

	@Inject(at = @At("HEAD"), method = "readAdditionalSaveData")
	private void lychee_readAdditionalSaveData(ValueInput input, CallbackInfo ci) {
		if (input.child(Lychee.ID).isEmpty()) {
			return;
		}
		Optional<ActionData.Builder> result = input.read(Lychee.ID, ActionData.Builder.CODEC);
		if (result.isEmpty()) {
			Lychee.LOGGER.error("Load Lychee action data: {}", input);
			lychee$self().discard();
			return;
		}
		lychee$data = result.get().build(level());
		var context = lychee$data.context();
		if (context.has(LycheeContextKey.RECIPE_ID, false)) {
			context.has(LycheeContextKey.RECIPE, true);
		}
		var lootParams = context.get(LycheeContextKey.LOOT_PARAMS);
		lootParams.set(LootContextParams.ORIGIN, lychee$self().position());
		try {
			lootParams.validate();
		} catch (IllegalArgumentException e) {
			Lychee.LOGGER.error("Load Lychee action data: {} -> {}", input, e.getMessage());
			lychee$self().discard();
		}
	}

	@Inject(at = @At("HEAD"), method = "addAdditionalSaveData")
	private void lychee_addAdditionalSaveData(ValueOutput output, CallbackInfo ci) {
		if (lychee$data != null) {
			ActionContext actionContext = lychee$data.actionContext();
			var builder = new ActionData.Builder(
					lychee$data.context(),
					lychee$data.delayedTicks(),
					actionContext.avoidDefault,
					actionContext.state,
					actionContext.jobs);
			output.store(Lychee.ID, ActionData.Builder.CODEC, builder);
		}
	}
}
