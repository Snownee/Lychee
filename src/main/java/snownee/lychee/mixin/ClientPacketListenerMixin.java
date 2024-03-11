package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import snownee.lychee.RecipeTypes;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

	// because tags are loaded after recipes, we bake cache here
	@Inject(at = @At("TAIL"), method = "handleUpdateTags")
	private void lychee_handleUpdateTags(final ClientboundUpdateTagsPacket packet, final CallbackInfo ci) {
		RecipeTypes.buildCache();
	}

}