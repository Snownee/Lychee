package snownee.lychee.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.world.item.crafting.RecipeAccess;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.ClientProxy;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

	@Shadow
	public abstract RecipeAccess recipes();

	// because tags are loaded after recipes, we bake cache here
	@Inject(at = @At("TAIL"), method = "handleUpdateTags")
	private void lychee_handleUpdateTags(final ClientboundUpdateTagsPacket packet, final CallbackInfo ci) {
		RecipeTypes.buildCache(ClientProxy.recipes(recipes()));
	}

}