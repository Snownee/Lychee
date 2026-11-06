package snownee.lychee.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.world.item.crafting.RecipeManager;
import snownee.lychee.RecipeTypes;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

	@Shadow
	public abstract RecipeManager getRecipeManager();

	// because tags are loaded after recipes, we bake cache here
	// in singleplayer the integrated server already built the cache, and the client and server share it.
	// rebuilding it here would overwrite the server result with the client one
	@Inject(at = @At("TAIL"), method = "handleUpdateTags")
	private void lychee_handleUpdateTags(final ClientboundUpdateTagsPacket packet, final CallbackInfo ci) {
		if (Minecraft.getInstance().hasSingleplayerServer()) {
			return;
		}
		RecipeTypes.buildCache(getRecipeManager());
	}

	// on a remote server, tags may arrive before the new recipes, so rebuild the cache once recipes are synced
	@Inject(at = @At("TAIL"), method = "handleUpdateRecipes")
	private void lychee_handleUpdateRecipes(final ClientboundUpdateRecipesPacket packet, final CallbackInfo ci) {
		if (Minecraft.getInstance().hasSingleplayerServer()) {
			return;
		}
		RecipeTypes.buildCache(getRecipeManager());
	}

}