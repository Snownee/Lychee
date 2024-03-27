package snownee.lychee.util;

import java.text.MessageFormat;
import java.util.List;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import snownee.lychee.PostActionTypes;
import snownee.lychee.client.core.post.CycleStatePropertyPostActionRenderer;
import snownee.lychee.client.core.post.IfPostActionRenderer;
import snownee.lychee.client.core.post.ItemBasedPostActionRenderer;
import snownee.lychee.client.core.post.ItemStackPostActionRenderer;
import snownee.lychee.client.core.post.PlaceBlockPostActionRenderer;
import snownee.lychee.client.core.post.PostActionRenderer;
import snownee.lychee.compat.IngredientInfo;
import snownee.lychee.core.post.DropItem;
import snownee.lychee.core.post.DropXp;
import snownee.lychee.core.post.Execute;
import snownee.lychee.core.post.Explode;
import snownee.lychee.core.post.Hurt;
import snownee.lychee.core.post.input.DamageItem;
import snownee.lychee.core.post.input.PreventDefault;
import snownee.lychee.core.post.input.SetItem;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.dripstone_dripping.DripstoneRecipeMod;
import snownee.lychee.dripstone_dripping.client.ParticleFactories;

public class ClientProxy implements ClientModInitializer {

	private static final Event<RecipeViewerWidgetClickListener> RECIPE_VIEWER_WIDGET_CLICK_EVENT = EventFactory.createArrayBacked(RecipeViewerWidgetClickListener.class, listeners -> (recipe, button) -> {
		for (RecipeViewerWidgetClickListener listener : listeners) {
			if (listener.onClick(recipe, button)) {
				return true;
			}
		}
		return false;
	});

	public static MutableComponent getDimensionDisplayName(ResourceKey<Level> dimension) {
		String key = Util.makeDescriptionId("dimension", dimension.location());
		if (I18n.exists(key)) {
			return Component.translatable(key);
		} else {
			return Component.literal(CommonProxy.capitaliseAllWords(dimension.location().getPath()));
		}
	}

	public static MutableComponent getStructureDisplayName(ResourceLocation rawName) {
		String key = Util.makeDescriptionId("structure", rawName);
		if (I18n.exists(key)) {
			return Component.translatable(key);
		} else {
			return Component.literal(CommonProxy.capitaliseAllWords(rawName.getPath()));
		}
	}

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

	public static boolean postInfoBadgeClickEvent(ILycheeRecipe<?> recipe, int button) {
		return RECIPE_VIEWER_WIDGET_CLICK_EVENT.invoker().onClick(recipe, button);
	}

	public static boolean isSinglePlayer() {
		return Minecraft.getInstance().getSingleplayerServer() != null;
	}

	@Override
	public void onInitializeClient() {
		ParticleFactoryRegistry.getInstance().register(DripstoneRecipeMod.DRIPSTONE_DRIPPING, $ -> new ParticleFactories.Dripping($));
		ParticleFactoryRegistry.getInstance().register(DripstoneRecipeMod.DRIPSTONE_FALLING, $ -> new ParticleFactories.Falling($));
		ParticleFactoryRegistry.getInstance().register(DripstoneRecipeMod.DRIPSTONE_SPLASH, $ -> new ParticleFactories.Splash($));

		PostActionRenderer.register(PostActionTypes.DROP_ITEM, (ItemStackPostActionRenderer<DropItem>) action -> action.stack);
		PostActionRenderer.register(PostActionTypes.SET_ITEM, (ItemStackPostActionRenderer<SetItem>) action -> action.stack);
		PostActionRenderer.register(PostActionTypes.DROP_XP, (ItemBasedPostActionRenderer<DropXp>) action -> Items.EXPERIENCE_BOTTLE.getDefaultInstance());
		PostActionRenderer.register(PostActionTypes.EXECUTE, (ItemBasedPostActionRenderer<Execute>) action -> Items.COMMAND_BLOCK.getDefaultInstance());
		PostActionRenderer.register(PostActionTypes.EXPLODE, (ItemBasedPostActionRenderer<Explode>) action -> Items.TNT.getDefaultInstance());
		PostActionRenderer.register(PostActionTypes.HURT, (ItemBasedPostActionRenderer<Hurt>) action -> Items.IRON_SWORD.getDefaultInstance());
		PostActionRenderer.register(PostActionTypes.IF, new IfPostActionRenderer());
		PostActionRenderer.register(PostActionTypes.PLACE, new PlaceBlockPostActionRenderer());
		PostActionRenderer.register(PostActionTypes.CYCLE_STATE_PROPERTY, new CycleStatePropertyPostActionRenderer());
		PostActionRenderer.register(PostActionTypes.DAMAGE_ITEM, new PostActionRenderer<>() {
			@Override
			public void loadCatalystsInfo(
					DamageItem action,
					final ILycheeRecipe<?> recipe,
					final List<IngredientInfo> ingredients) {
				String key = CommonProxy.makeDescriptionId("postAction", PostActionTypes.DAMAGE_ITEM.getRegistryName());
				Component component = Component.translatable(key, action.damage).withStyle(ChatFormatting.YELLOW);
				Minecraft mc = Minecraft.getInstance();
				recipe.getItemIndexes(action.target).forEach(i -> {
					IngredientInfo info = ingredients.get(i);
					info.addTooltip(component);
					action.getConditionTooltips(info.tooltips, 0, mc.level, mc.player);
					info.isCatalyst = true;
				});
			}
		});
		PostActionRenderer.register(PostActionTypes.PREVENT_DEFAULT, new PostActionRenderer<>() {
			@Override
			public void loadCatalystsInfo(
					PreventDefault action,
					final ILycheeRecipe<?> recipe,
					final List<IngredientInfo> ingredients) {
				if (!(recipe instanceof LycheeRecipe<?> lycheeRecipe) ||
					!lycheeRecipe.getType().canPreventConsumeInputs) {return;}
				Minecraft mc = Minecraft.getInstance();
				for (var ingredient : ingredients) {
					if (!ingredient.tooltips.isEmpty()) {
						continue;
					}
					ingredient.addTooltip(lycheeRecipe.getType().getPreventDefaultDescription(lycheeRecipe));
					action.getConditionTooltips(ingredient.tooltips, 0, mc.level, mc.player);
					ingredient.isCatalyst = true;
				}
			}
		});
	}

	@FunctionalInterface
	public interface RecipeViewerWidgetClickListener {
		boolean onClick(ILycheeRecipe<?> recipe, int button);
	}
}
