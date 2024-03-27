package snownee.lychee.util;

import java.text.MessageFormat;
import java.util.List;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;
import snownee.kiwi.util.KEvent;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.action.DropItem;
import snownee.lychee.action.DropXp;
import snownee.lychee.action.Execute;
import snownee.lychee.action.Explode;
import snownee.lychee.action.Hurt;
import snownee.lychee.action.input.DamageItem;
import snownee.lychee.action.input.PreventDefault;
import snownee.lychee.action.input.SetItem;
import snownee.lychee.client.action.CycleStatePropertyPostActionRenderer;
import snownee.lychee.client.action.IfPostActionRenderer;
import snownee.lychee.client.action.PlaceBlockPostActionRenderer;
import snownee.lychee.compat.IngredientInfo;
import snownee.lychee.util.action.ItemBasedPostActionRenderer;
import snownee.lychee.util.action.ItemStackPostActionRenderer;
import snownee.lychee.util.action.PostActionRenderer;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.particles.dripstone.DripstoneParticleService;
import snownee.lychee.util.particles.dripstone.client.ParticleFactories;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class ClientProxy implements ClientModInitializer {

	private static final KEvent<RecipeViewerWidgetClickListener> RECIPE_VIEWER_WIDGET_CLICK_EVENT =
			KEvent.createArrayBacked(RecipeViewerWidgetClickListener.class, listeners -> (recipe, button) -> {
				for (var listener : listeners) {
					if (listener.onClick(recipe, button)) {
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

	public static boolean postInfoBadgeClickEvent(ILycheeRecipe recipe, int button) {
		return RECIPE_VIEWER_WIDGET_CLICK_EVENT.invoker().onClick(recipe, button);
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

		PostActionRenderer.register(
				PostActionTypes.DROP_ITEM,
				(ItemStackPostActionRenderer<DropItem>) DropItem::stack
		);
		PostActionRenderer.register(
				PostActionTypes.SET_ITEM,
				(ItemStackPostActionRenderer<SetItem>) SetItem::stack
		);
		PostActionRenderer.register(
				PostActionTypes.DROP_XP,
				(ItemBasedPostActionRenderer<DropXp>) action -> Items.EXPERIENCE_BOTTLE.getDefaultInstance()
		);
		PostActionRenderer.register(
				PostActionTypes.EXECUTE,
				(ItemBasedPostActionRenderer<Execute>) action -> Items.COMMAND_BLOCK.getDefaultInstance()
		);
		PostActionRenderer.register(
				PostActionTypes.EXPLODE,
				(ItemBasedPostActionRenderer<Explode>) action -> Items.TNT.getDefaultInstance()
		);
		PostActionRenderer.register(
				PostActionTypes.HURT,
				(ItemBasedPostActionRenderer<Hurt>) action -> Items.IRON_SWORD.getDefaultInstance()
		);
		PostActionRenderer.register(PostActionTypes.IF, new IfPostActionRenderer());
		PostActionRenderer.register(PostActionTypes.PLACE, new PlaceBlockPostActionRenderer());
		PostActionRenderer.register(PostActionTypes.CYCLE_STATE_PROPERTY, new CycleStatePropertyPostActionRenderer());
		PostActionRenderer.register(PostActionTypes.DAMAGE_ITEM, new PostActionRenderer<>() {
			@Override
			public void loadCatalystsInfo(
					DamageItem action,
					final ILycheeRecipe<?> recipe,
					final List<IngredientInfo> ingredients) {
				var key = CommonProxy.makeDescriptionId("postAction", LycheeRegistries.POST_ACTION.getKey(action.type()));
				var component = Component.translatable(key, action.damage()).withStyle(ChatFormatting.YELLOW);
				var mc = Minecraft.getInstance();
				recipe.getItemIndexes(action.target()).forEach(i -> {
					var info = ingredients.get(i);
					info.addTooltip(component);
					action.conditions().appendToTooltips(info.tooltips, mc.level, mc.player, 0);
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
				if (recipe == null ||
						!(recipe.getType() instanceof LycheeRecipeType<?, ?> lycheeRecipeType) ||
						!lycheeRecipeType.canPreventConsumeInputs) {
					return;
				}
				var mc = Minecraft.getInstance();
				for (var ingredient : ingredients) {
					if (!ingredient.tooltips.isEmpty()) {
						continue;
					}
					ingredient.addTooltip(((LycheeRecipeType) lycheeRecipeType).getPreventDefaultDescription(recipe));
					action.conditions().appendToTooltips(ingredient.tooltips, mc.level, mc.player, 0);
					ingredient.isCatalyst = true;
				}
			}
		});
	}

	@FunctionalInterface
	public interface RecipeViewerWidgetClickListener {
		boolean onClick(ILycheeRecipe recipe, int button);
	}
}
