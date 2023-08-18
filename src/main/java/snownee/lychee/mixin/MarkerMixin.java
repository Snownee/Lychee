package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.gson.JsonObject;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.item.crafting.Recipe;
import snownee.lychee.Lychee;
import snownee.lychee.core.ActionRuntime.State;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.post.Delay.LycheeMarker;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.fragment.Fragments;
import snownee.lychee.util.CommonProxy;

@Mixin(Marker.class)
public class MarkerMixin implements LycheeMarker {

	@Unique
	private int lychee$ticks;
	@Unique
	private ILycheeRecipe<?> lychee$recipe;
	@Unique
	private LycheeContext lychee$ctx;

	@Override
	public void lychee$setContext(ILycheeRecipe<?> recipe, LycheeContext ctx) {
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

	@Inject(at = @At("HEAD"), method = "readAdditionalSaveData")
	private void lychee_readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
		if (compoundTag.contains("lychee:ctx")) {
			lychee$ticks = compoundTag.getInt("lychee:ticks");
			lychee$ctx = LycheeContext.load(Fragments.GSON.fromJson(compoundTag.getString("lychee:ctx"), JsonObject.class), this);
			ResourceLocation recipeId = ResourceLocation.tryParse(compoundTag.getString("lychee:recipe"));
			Recipe<?> recipe = CommonProxy.recipe(recipeId);
			if (recipe instanceof ILycheeRecipe) {
				lychee$recipe = (ILycheeRecipe<?>) recipe;
			}
		}
		if (lychee$recipe == null && getEntity().hasCustomName() && Lychee.ID.equals(getEntity().getCustomName().getString())) {
			getEntity().discard();
		}
	}

	@Inject(at = @At("HEAD"), method = "addAdditionalSaveData")
	private void lychee_addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
		if (lychee$recipe == null || lychee$ctx == null) {
			return;
		}
		compoundTag.putInt("lychee:ticks", lychee$ticks);
		compoundTag.putString("lychee:ctx", lychee$ctx.save().toString());
		compoundTag.putString("lychee:recipe", lychee$recipe.lychee$getId().toString());
	}

}
