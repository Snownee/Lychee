package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.item.crafting.RecipeManager;
import snownee.lychee.RecipeTypes;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {

	@Shadow
	@Final
	private RecipeManager recipes;

	// because tags are loaded after recipes, we bake cache here
	@Inject(
			method = "updateRegistryTags()V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Blocks;rebuildCache()V"))
	private void lychee_updateRegistryTags(CallbackInfo ci) {
		RecipeTypes.buildCache(recipes);
	}

}
