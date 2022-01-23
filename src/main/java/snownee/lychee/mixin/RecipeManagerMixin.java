package snownee.lychee.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.gson.JsonElement;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import snownee.lychee.Lychee;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {

	@Inject(at = @At("TAIL"), method = "apply")
	private void lychee_apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler, CallbackInfo ci) {
		Lychee.onRecipesLoaded((RecipeManager) (Object) this);
	}

	@Inject(at = @At("TAIL"), method = "replaceRecipes")
	private void lychee_replaceRecipes(Iterable<Recipe<?>> pRecipes, CallbackInfo ci) {
		Lychee.onRecipesReplaced((RecipeManager) (Object) this, pRecipes);
	}

}
