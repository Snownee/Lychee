package snownee.lychee.mixin;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import snownee.lychee.RecipeTypes;
import snownee.lychee.fragment.Fragments;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {

	// because tags are loaded after recipes, we bake cache here
	@Inject(at = @At("TAIL"), method = "updateRegistryTags(Lnet/minecraft/core/RegistryAccess;)V")
	private void lychee_updateRegistryTags(CallbackInfo ci) {
		RecipeTypes.buildCache();
	}

	@Inject(
			at = @At(
					value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;onResourceReload(Lnet/minecraft/server/ReloadableServerResources;Lnet/minecraft/core/RegistryAccess;)Ljava/util/List;", remap = false
			), method = "loadResources", locals = LocalCapture.CAPTURE_FAILEXCEPTION
	)
	private static void lychee_loadResources(ResourceManager p_248588_, RegistryAccess.Frozen p_251163_, FeatureFlagSet p_250212_, Commands.CommandSelection p_249301_, int p_251126_, Executor p_249136_, Executor p_249601_, CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir, ReloadableServerResources reloadableserverresources, List<PreparableReloadListener> listeners) {
		listeners.add(0, Fragments.INSTANCE);
	}

}
