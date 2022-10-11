package snownee.lychee.dripstone_dripping.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import snownee.lychee.Lychee;
import snownee.lychee.dripstone_dripping.DripstoneRecipeMod;

public class DripstoneRecipeModClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		ClientSpriteRegistryCallback.event(InventoryMenu.BLOCK_ATLAS).register(((atlasTexture, registry) -> {
			for (int i = 0; i <= 3; i++) {
				registry.register(new ResourceLocation(Lychee.ID, "particle/splash_" + i));
			}
		}));

		ParticleFactoryRegistry.getInstance().register(DripstoneRecipeMod.DRIPSTONE_DRIPPING, ParticleFactories.Dripping::new);
		ParticleFactoryRegistry.getInstance().register(DripstoneRecipeMod.DRIPSTONE_FALLING, ParticleFactories.Falling::new);
		ParticleFactoryRegistry.getInstance().register(DripstoneRecipeMod.DRIPSTONE_SPLASH, ParticleFactories.Splash::new);
	}

}