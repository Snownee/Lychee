package snownee.lychee.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import snownee.kiwi.util.resource.OneTimeLoader;
import snownee.lychee.LycheeConfig;
import snownee.lychee.util.json.JsonFragmentManager;

@Mixin(value = RecipeManager.class, priority = 333)
public class RecipeManagerMixin {
	@Unique
	private static final ThreadLocal<JsonFragmentManager> fragmentManagerProvider = new ThreadLocal<>();

	@Inject(
			method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
			at = @At("HEAD"))
	private void lychee_beginApply(
			Map<ResourceLocation, JsonElement> object,
			ResourceManager resourceManager,
			ProfilerFiller profiler,
			CallbackInfo ci) {
		if (LycheeConfig.enableYamlRecipes) {
			Map<ResourceLocation, JsonElement> yamlRecipes = OneTimeLoader.load(
					resourceManager,
					Registries.elementsDirPath(Registries.RECIPE),
					ExtraCodecs.JSON);
			for (Map.Entry<ResourceLocation, JsonElement> entry : yamlRecipes.entrySet()) {
				object.putIfAbsent(entry.getKey(), entry.getValue());
			}
		}
		if (LycheeConfig.enableFragment) {
			JsonFragmentManager fragmentManager = new JsonFragmentManager(resourceManager);
			fragmentManagerProvider.set(fragmentManager);
			object.values().forEach(fragmentManager::process);
		}
	}

	@Inject(
			method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
			at = @At("RETURN"))
	private void lychee_endApply(
			Map<ResourceLocation, JsonElement> object,
			ResourceManager resourceManager,
			ProfilerFiller profiler,
			CallbackInfo ci) {
		if (LycheeConfig.enableFragment) {
			fragmentManagerProvider.remove();
		}
	}

	// though we have processed fragments in apply, we still need to handle calls from other mods
	@Inject(method = "fromJson", at = @At("HEAD"))
	private static void lychee_fromJson(
			final ResourceLocation resourceLocation,
			final JsonObject json,
			final HolderLookup.Provider provider,
			final CallbackInfoReturnable<RecipeHolder<?>> cir) {
		if (LycheeConfig.enableFragment && json != null) {
			JsonFragmentManager fragmentManager = fragmentManagerProvider.get();
			if (fragmentManager != null) {
				fragmentManager.process(json);
			}
		}
	}
}
