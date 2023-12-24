package snownee.lychee.mixin.action;

import net.minecraft.world.item.crafting.RecipeHolder;

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
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.util.action.ActionMarker;
import snownee.lychee.fragment.Fragments;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.ActionRuntime.State;
import snownee.lychee.util.recipe.OldLycheeRecipe;

@Mixin(Marker.class)
public class MarkerMixin implements ActionMarker {

	@Unique
	private int lychee$ticks;
	@Unique
	private RecipeHolder<OldLycheeRecipe<?>> lychee$recipe;
	@Unique
	private LycheeRecipeContext lychee$ctx;

	@Override
	public void lychee$setContext(RecipeHolder<OldLycheeRecipe<?>> recipe, LycheeRecipeContext ctx) {
		lychee$ctx = ctx;
		lychee$recipe = recipe;
	}

	@Override
	public LycheeRecipeContext lychee$getContext() {
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
			self().discard();
		}
	}

	@Inject(at = @At("HEAD"), method = "readAdditionalSaveData")
	private void lychee_readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
		if (compoundTag.contains("lychee:ctx")) {
			lychee$ticks = compoundTag.getInt("lychee:ticks");
			lychee$ctx = LycheeRecipeContext.load(Fragments.GSON.fromJson(
					compoundTag.getString("lychee:ctx"),
					JsonObject.class
			), this);
			ResourceLocation recipeId = ResourceLocation.tryParse(compoundTag.getString("lychee:recipe"));
			Recipe<?> recipe = CommonProxy.recipe(recipeId);
			if (recipe instanceof OldLycheeRecipe<?> lycheeRecipe) {
				lychee$recipe = new RecipeHolder<>(recipeId, lycheeRecipe);
			}
		}
		if (lychee$recipe == null && self().hasCustomName()
			&& Lychee.ID.equals(self().getCustomName().getString())) {
			self().discard();
		}
	}

	@Inject(at = @At("HEAD"), method = "addAdditionalSaveData")
	private void lychee_addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
		if (lychee$recipe == null || lychee$ctx == null) {
			return;
		}
		compoundTag.putInt("lychee:ticks", lychee$ticks);
		compoundTag.putString("lychee:ctx", lychee$ctx.save().toString());
		compoundTag.putString("lychee:recipe", lychee$recipe.id().toString());
	}
}
