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
					value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;onResourceReload(Lnet/minecraft/server/ReloadableServerResources;)Ljava/util/List;", remap = false
			), method = "loadResources", locals = LocalCapture.CAPTURE_FAILEXCEPTION
	)
	private static void lychee_loadResources(ResourceManager p_206862_, RegistryAccess.Frozen p_206863_, Commands.CommandSelection p_206864_, int p_206865_, Executor p_206866_, Executor p_206867_, CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> ci, ReloadableServerResources reloadableserverresources, List<PreparableReloadListener> listeners) {
		listeners.add(0, Fragments.INSTANCE);
	}

}
