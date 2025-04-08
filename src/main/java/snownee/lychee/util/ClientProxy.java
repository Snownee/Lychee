package snownee.lychee.util;

import java.text.MessageFormat;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import snownee.kiwi.util.KEvent;
import snownee.lychee.Lychee;
import snownee.lychee.util.action.ActionRenderer;
import snownee.lychee.util.particles.dripstone.DripstoneParticleService;
import snownee.lychee.util.particles.dripstone.client.ParticleFactories;
import snownee.lychee.util.ui.ElementRenderer;

@Mod(value = Lychee.ID, dist = Dist.CLIENT)
public class ClientProxy {

	public static final boolean HAS_PONDER = ModList.get().isLoaded("ponder");
	private static final KEvent<RecipeViewerWidgetClickListener> RECIPE_VIEWER_WIDGET_CLICK_EVENT =
			KEvent.createArrayBacked(
					RecipeViewerWidgetClickListener.class, listeners -> (recipe, location, button) -> {
						for (var listener : listeners) {
							if (listener.onClick(recipe, location, button)) {
								return true;
							}
						}
						return false;
					});

	public static MutableComponent format(String s, Object... objects) {
		try {
			return Component.literal(MessageFormat.format(I18n.get(s), objects));
		} catch (Exception e) {
			return Component.translatable(s, objects);
		}
	}

	public static void registerWidgetClickListener(RecipeViewerWidgetClickListener listener) {
		RECIPE_VIEWER_WIDGET_CLICK_EVENT.register(listener);
	}

	public static boolean postWidgetClickEvent(Recipe<?> recipe, String id, int button) {
		return RECIPE_VIEWER_WIDGET_CLICK_EVENT.invoker().onClick(recipe, id, button);
	}

	public ClientProxy(IEventBus modEventBus) {
		ParticleFactoryRegistry.getInstance().register(
				DripstoneParticleService.DRIPSTONE_DRIPPING,
				ParticleFactories.Dripping::new
		);
		ParticleFactoryRegistry.getInstance().register(
				DripstoneParticleService.DRIPSTONE_FALLING,
				ParticleFactories.Falling::new
		);
		ParticleFactoryRegistry.getInstance().register(
				DripstoneParticleService.DRIPSTONE_SPLASH,
				ParticleFactories.Splash::new
		);

		ActionRenderer.init();
		ElementRenderer.init();
	}

	@FunctionalInterface
	public interface RecipeViewerWidgetClickListener {
		boolean onClick(Recipe<?> recipe, String id, int button);
	}
}
