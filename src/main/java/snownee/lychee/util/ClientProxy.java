package snownee.lychee.util;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import snownee.kiwi.util.KEvent;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.action.DropItem;
import snownee.lychee.action.DropXp;
import snownee.lychee.action.Execute;
import snownee.lychee.action.Explode;
import snownee.lychee.action.PlaceBlock;
import snownee.lychee.action.SetBlock;
import snownee.lychee.action.input.DamageItem;
import snownee.lychee.action.input.PreventDefault;
import snownee.lychee.action.input.SetItem;
import snownee.lychee.client.action.IfActionRenderer;
import snownee.lychee.compat.recipeviewer.IngredientInfo;
import snownee.lychee.compat.recipeviewer.SlotType;
import snownee.lychee.util.action.ActionRenderer;
import snownee.lychee.util.action.BlockBasedActionRenderer;
import snownee.lychee.util.action.ItemBasedActionRenderer;
import snownee.lychee.util.action.ItemStackActionRenderer;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.particles.dripstone.DripstoneParticleService;
import snownee.lychee.util.particles.dripstone.client.ParticleFactories;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

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

		ActionRenderer.register(
				PostActionTypes.DROP_ITEM,
				(ItemStackActionRenderer<DropItem>) DropItem::stack
		);
		ActionRenderer.register(
				PostActionTypes.SET_ITEM,
				(ItemStackActionRenderer<SetItem>) SetItem::stack
		);
		ActionRenderer.register(
				PostActionTypes.DROP_XP,
				(ItemBasedActionRenderer<DropXp>) action -> Items.EXPERIENCE_BOTTLE.getDefaultInstance()
		);
		ActionRenderer.register(
				PostActionTypes.EXECUTE,
				(ItemBasedActionRenderer<Execute>) action -> Items.COMMAND_BLOCK.getDefaultInstance()
		);
		ActionRenderer.register(
				PostActionTypes.EXPLODE,
				(ItemBasedActionRenderer<Explode>) action -> Items.TNT.getDefaultInstance()
		);
		ActionRenderer.register(PostActionTypes.IF, new IfActionRenderer());
		ActionRenderer.register(PostActionTypes.PLACE, BlockBasedActionRenderer.fromPredicate(PlaceBlock::block));
		ActionRenderer.register(PostActionTypes.SET_BLOCK, BlockBasedActionRenderer.fromPredicate(SetBlock::block));
		ActionRenderer.register(
				PostActionTypes.CYCLE_STATE_PROPERTY, new BlockBasedActionRenderer<>(it -> {
					var blockStates = BlockPredicateExtensions.getShowcaseBlockStates(it.block(), Set.of(it.property()));
					return CommonProxy.getCycledItem(blockStates, Blocks.AIR.defaultBlockState(), 1000);
				}));
		ActionRenderer.register(
				PostActionTypes.DAMAGE_ITEM, new ActionRenderer<>() {
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
							info.type = SlotType.CATALYST;
						});
					}
				});
		ActionRenderer.register(
				PostActionTypes.PREVENT_DEFAULT, new ActionRenderer<>() {
					@Override
					public void loadCatalystsInfo(
							PreventDefault action,
							final ILycheeRecipe<?> recipe,
							final List<IngredientInfo> ingredients) {
						if (recipe == null ||
								!(recipe.getType() instanceof LycheeRecipeType<?> lycheeRecipeType) ||
								!lycheeRecipeType.canPreventConsumeInputs) {
							return;
						}
						var mc = Minecraft.getInstance();
						for (var info : ingredients) {
							if (!info.tooltips.isEmpty()) {
								continue;
							}
							info.addTooltip(((LycheeRecipeType) lycheeRecipeType).getPreventDefaultDescription(recipe));
							action.conditions().appendToTooltips(info.tooltips, mc.level, mc.player, 0);
							info.type = SlotType.CATALYST;
						}
					}
				});
	}

	@FunctionalInterface
	public interface RecipeViewerWidgetClickListener {
		boolean onClick(ILycheeRecipe<?> recipe, @Nullable ResourceLocation id, int button);
	}
}
