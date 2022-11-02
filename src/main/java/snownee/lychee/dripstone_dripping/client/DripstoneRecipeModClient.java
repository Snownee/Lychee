package snownee.lychee.dripstone_dripping.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import snownee.lychee.Lychee;
import snownee.lychee.dripstone_dripping.DripstoneRecipeMod;

public class DripstoneRecipeModClient {

	public static void onInitializeClient() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
		eventBus.addListener(DripstoneRecipeModClient::addSprites);
		eventBus.addListener(DripstoneRecipeModClient::addParticleProviders);
	}

	public static void addSprites(TextureStitchEvent.Pre event) {
		if (event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
			for (int i = 0; i <= 3; i++) {
				event.addSprite(new ResourceLocation(Lychee.ID, "particle/splash_" + i));
			}
		}
	}

	public static void addParticleProviders(RegisterParticleProvidersEvent event) {
		event.register(DripstoneRecipeMod.DRIPSTONE_DRIPPING, ParticleFactories.Dripping::new);
		event.register(DripstoneRecipeMod.DRIPSTONE_FALLING, ParticleFactories.Falling::new);
		event.register(DripstoneRecipeMod.DRIPSTONE_SPLASH, ParticleFactories.Splash::new);
	}

}