package snownee.lychee.mixin;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.crafting.RecipeManager;
import snownee.kiwi.util.resource.AlternativesFileToIdConverter;
import snownee.kiwi.util.resource.OneTimeLoader;
import snownee.lychee.LycheeConfig;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.json.JsonFragmentManager;

@Mixin(SimpleJsonResourceReloadListener.class)
public class SimpleJsonResourceReloadListenerMixin {
	@Shadow
	@Final
	private static Logger LOGGER;

	@Inject(
			method = "scanDirectory(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/FileToIdConverter;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;)V",
			at = @At("HEAD"))
	private static <T> void lychee_beginApply_scanDirectory(
			ResourceManager manager,
			FileToIdConverter lister,
			DynamicOps<JsonElement> ops,
			Codec<T> codec,
			Map<Identifier, T> result,
			CallbackInfo ci) {
		lychee_beginApply(manager, lister, ops, codec, result);
	}

	@Inject(method = "scanDirectoryWithModifier", at = @At("HEAD"))
	private static <T> void lychee_beginApply_scanDirectoryWithModifier(
			ResourceManager manager,
			FileToIdConverter lister,
			DynamicOps<JsonElement> ops,
			Codec<T> codec,
			Map<Identifier, T> result,
			Consumer<Map<Identifier, JsonElement>> jsonConsumer,
			CallbackInfo ci) {
		lychee_beginApply(manager, lister, ops, codec, result);
	}

	@Unique
	private static <T> void lychee_beginApply(
			ResourceManager manager,
			FileToIdConverter lister,
			DynamicOps<JsonElement> ops,
			Codec<T> codec,
			Map<Identifier, T> result) {
		if (lister != RecipeManager.RECIPE_LISTER) {
			return;
		}
		JsonFragmentManager fragmentManager = null;
		if (LycheeConfig.enableFragment) {
			fragmentManager = new JsonFragmentManager(manager);
			CommonProxy.fragmentManagerProvider.set(fragmentManager);
		}
		if (LycheeConfig.enableYamlRecipes) {
			AlternativesFileToIdConverter yamlLister = new AlternativesFileToIdConverter(
					Registries.elementsDirPath(Registries.RECIPE),
					List.of(".yaml"));
			RegistryOps.@Nullable RegistryInfoLookup registryInfo = null;
			if (ops instanceof RegistryOps<?> registryOps) {
				registryInfo = registryOps.lookupProvider;
			}
			Map<Identifier, JsonElement> yamlRecipes = OneTimeLoader.load(
					manager,
					yamlLister,
					ExtraCodecs.JSON,
					new OneTimeLoader.Context(registryInfo));
			if (fragmentManager != null) {
				yamlRecipes.values().forEach(fragmentManager::process);
			}
			for (Map.Entry<Identifier, JsonElement> entry : yamlRecipes.entrySet()) {
				Identifier id = entry.getKey();
				Identifier location = yamlLister.idToFile(id);
				try {
					codec.parse(ops, entry.getValue()).ifSuccess(parsed -> {
						if (result.putIfAbsent(id, parsed) != null) {
							throw new IllegalStateException("Duplicate data file ignored with ID " + id);
						}
					}).ifError(error -> LOGGER.error("Couldn't parse data file '{}' from '{}': {}", id, location, error));
				} catch (IllegalArgumentException | JsonParseException e) {
					LOGGER.error("Couldn't parse data file '{}' from '{}'", id, location, e);
				}
			}
		}
	}


	@Inject(
			method = "scanDirectory(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/FileToIdConverter;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;)V",
			at = @At("HEAD"))
	private static <T> void lychee_endApply(
			ResourceManager manager,
			FileToIdConverter lister,
			DynamicOps<JsonElement> ops,
			Codec<T> codec,
			Map<Identifier, T> result,
			CallbackInfo ci) {
		if (LycheeConfig.enableFragment) {
			CommonProxy.fragmentManagerProvider.remove();
		}
	}
}