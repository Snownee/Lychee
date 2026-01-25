package snownee.lychee.mixin;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.JsonObject;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import snownee.lychee.LycheeConfig;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.json.JsonFragmentManager;

@Mixin(value = RecipeManager.class, priority = 333)
public class RecipeManagerMixin {
	// though we have processed fragments in SimpleJsonResourceReloadListenerMixin, we still need to handle calls from other mods
	@Inject(method = "fromJson", at = @At("HEAD"))
	private static void lychee_fromJson(
			ResourceKey<Recipe<?>> id,
			@Nullable JsonObject object,
			HolderLookup.Provider registries,
			CallbackInfoReturnable<RecipeHolder<?>> cir) {
		if (LycheeConfig.enableFragment && object != null) {
			JsonFragmentManager fragmentManager = CommonProxy.fragmentManagerProvider.get();
			if (fragmentManager != null) {
				fragmentManager.process(object);
			}
		}
	}
}
