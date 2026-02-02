package snownee.lychee.util;

import java.text.MessageFormat;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.material.Fluid;
import snownee.kiwi.loader.Platform;
import snownee.kiwi.util.KEvent;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.util.action.ActionRenderer;
import snownee.lychee.util.particles.dripstone.DripstoneParticleService;
import snownee.lychee.util.particles.dripstone.client.ParticleFactories;
import snownee.lychee.util.ui.ElementRenderer;
import snownee.lychee.util.ui.InputAction;

public class ClientProxy implements ClientModInitializer {

	private static final KEvent<RecipeViewerWidgetInputListener> RECIPE_VIEWER_WIDGET_INPUT_EVENT =
			KEvent.createArrayBacked(
					RecipeViewerWidgetInputListener.class, listeners -> (recipe, location, action) -> {
						for (var listener : listeners) {
							if (listener.on(recipe, location, action)) {
								return true;
							}
						}
						return false;
					});
	public static boolean hasJade = Platform.isModLoaded("jade");

	public static MutableComponent format(String s, Object... objects) {
		try {
			return Component.literal(MessageFormat.format(I18n.get(s), objects));
		} catch (Exception e) {
			return Component.translatable(s, objects);
		}
	}

	public static void registerWidgetInputListener(RecipeViewerWidgetInputListener listener) {
		RECIPE_VIEWER_WIDGET_INPUT_EVENT.register(listener);
	}

	public static boolean postWidgetInputEvent(
			Recipe<?> recipe,
			String id,
			InputAction action,
			@Nullable InteractiveRenderElement element) {
		return action.isMouseOver(element) && RECIPE_VIEWER_WIDGET_INPUT_EVENT.invoker().on(recipe, id, action);
	}

	public static Component getFluidName(Fluid fluid) {
		return FluidVariantAttributes.getName(FluidVariant.of(fluid));
	}

	@Nullable
	public static RecipeHolder<?> recipe(ResourceKey<Recipe<?>> id) {
		return Objects.requireNonNull(Minecraft.getInstance().getConnection()).recipes().getSynchronizedRecipes().get(id);
	}

	public static RecipeMap recipes(RecipeAccess recipes) {
		return RecipeMap.create(recipes.getSynchronizedRecipes().recipes());
	}

	@Override
	public void onInitializeClient() {
		ParticleProviderRegistry.getInstance().register(
				DripstoneParticleService.DRIPSTONE_DRIPPING,
				ParticleFactories.Dripping::new
		);
		ParticleProviderRegistry.getInstance().register(
				DripstoneParticleService.DRIPSTONE_FALLING,
				ParticleFactories.Falling::new
		);
		ParticleProviderRegistry.getInstance().register(
				DripstoneParticleService.DRIPSTONE_SPLASH,
				ParticleFactories.Splash::new
		);

		ActionRenderer.init();
		ElementRenderer.init();
	}

	@FunctionalInterface
	public interface RecipeViewerWidgetInputListener {
		boolean on(Recipe<?> recipe, String id, InputAction action);
	}
}
