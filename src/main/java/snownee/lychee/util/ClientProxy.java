package snownee.lychee.util;

import java.text.MessageFormat;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import snownee.kiwi.util.KEvent;
import snownee.lychee.util.action.ActionRenderer;
import snownee.lychee.util.particles.dripstone.DripstoneParticleService;
import snownee.lychee.util.particles.dripstone.client.ParticleFactories;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.ui.ElementRenderer;

public class ClientProxy implements ClientModInitializer {

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

	public static void registerInfoBadgeClickListener(RecipeViewerWidgetClickListener listener) {
		RECIPE_VIEWER_WIDGET_CLICK_EVENT.register(listener);
	}

	public static boolean postInfoBadgeClickEvent(ILycheeRecipe<?> recipe, @Nullable ResourceLocation id, int button) {
		return RECIPE_VIEWER_WIDGET_CLICK_EVENT.invoker().onClick(recipe, id, button);
	}

	public static void drawCenteredStringNoShadow(GuiGraphics graphics, Font font, Component text, int x, int y, int color) {
		FormattedCharSequence formattedCharSequence = text.getVisualOrderText();
		graphics.drawString(font, formattedCharSequence, x - font.width(formattedCharSequence) / 2, y, color, false);
	}

	@Override
	public void onInitializeClient() {
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
		boolean onClick(ILycheeRecipe<?> recipe, @Nullable ResourceLocation id, int button);
	}
}
