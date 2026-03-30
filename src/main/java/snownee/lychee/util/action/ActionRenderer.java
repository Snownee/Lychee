package snownee.lychee.util.action;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.action.DropItem;
import snownee.lychee.action.DropXp;
import snownee.lychee.action.Execute;
import snownee.lychee.action.Explode;
import snownee.lychee.action.PlaceBlock;
import snownee.lychee.action.RandomSelect;
import snownee.lychee.action.SetBlock;
import snownee.lychee.action.input.DamageItem;
import snownee.lychee.action.input.PreventDefault;
import snownee.lychee.action.input.SetItem;
import snownee.lychee.client.action.IfActionRenderer;
import snownee.lychee.compat.recipeviewer.IngredientInfo;
import snownee.lychee.compat.recipeviewer.SlotType;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public interface ActionRenderer<T extends PostAction> {

	Map<PostActionType<?>, ActionRenderer<?>> RENDERERS = Maps.newIdentityHashMap();
	ActionRenderer<PostAction> DEFAULT = new ActionRenderer<>() {};

	static void init() {
		register(PostActionTypes.DROP_ITEM, (ItemStackActionRenderer<DropItem>) DropItem::itemStack);
		register(PostActionTypes.SET_ITEM, (ItemStackActionRenderer<SetItem>) $ -> Objects.requireNonNull($.itemStack()));
		register(PostActionTypes.DROP_XP, (ItemBasedActionRenderer<DropXp>) _ -> new ItemStackTemplate(Items.EXPERIENCE_BOTTLE));
		register(PostActionTypes.EXECUTE, (ItemBasedActionRenderer<Execute>) _ -> new ItemStackTemplate(Items.COMMAND_BLOCK));
		register(PostActionTypes.EXPLODE, (ItemBasedActionRenderer<Explode>) _ -> new ItemStackTemplate(Items.TNT));
		register(PostActionTypes.IF, new IfActionRenderer());
		register(PostActionTypes.PLACE, BlockBasedActionRenderer.fromPredicate(PlaceBlock::block));
		register(PostActionTypes.SET_BLOCK, BlockBasedActionRenderer.fromPredicate(SetBlock::block));
		register(
				PostActionTypes.CYCLE_STATE_PROPERTY, new BlockBasedActionRenderer<>(it -> {
					var blockStates = BlockPredicateExtensions.getShowcaseBlockStates(it.block(), Set.of(it.property()));
					return CommonProxy.getCycledItem(blockStates, Blocks.AIR.defaultBlockState(), 1000);
				}));
		register(
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
							info.relatedAction = action;
						});
					}
				});
		register(
				PostActionTypes.PREVENT_DEFAULT, new ActionRenderer<>() {
					@Override
					public void loadCatalystsInfo(
							PreventDefault action,
							final ILycheeRecipe<?> recipe,
							final List<IngredientInfo> ingredients) {
						if (!(recipe.getType() instanceof LycheeRecipeType<?> lycheeRecipeType) ||
								!lycheeRecipeType.canPreventConsumeInputs) {
							return;
						}
						var mc = Minecraft.getInstance();
						for (var info : ingredients) {
							if (!info.tooltips.isEmpty()) {
								continue;
							}
							//noinspection unchecked,rawtypes
							info.addTooltip(((LycheeRecipeType) lycheeRecipeType).getPreventDefaultDescription(recipe));
							action.conditions().appendToTooltips(info.tooltips, mc.level, mc.player, 0);
							info.type = SlotType.CATALYST;
							info.relatedAction = action;
						}
					}
				});
	}

	static <T extends PostAction> ActionRenderer<T> of(PostAction action) {
		//noinspection unchecked
		return (ActionRenderer<T>) Objects.requireNonNull(RENDERERS.getOrDefault(action.type(), DEFAULT));
	}

	static <T extends PostAction> void register(PostActionType<T> type, ActionRenderer<T> renderer) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(renderer);
		RENDERERS.put(type, renderer);
	}

	static List<Component> getTooltipsFromRandom(RandomSelect randomSelect, PostAction child, @Nullable Player player) {
		var index = -1;
		for (int i = 0; i < randomSelect.entries().size(); i++) {
			if (randomSelect.entries().get(i).action().equals(child)) {
				index = i;
			}
		}
		var list = randomSelect.entries().size() == 1 && randomSelect.emptyWeight() == 0 ?
				Lists.newArrayList(randomSelect.getDisplayName()) :
				ActionRenderer.of(child).getBaseTooltips(child, player);
		if (index == -1) {
			return list; //TODO nested actions?
		}
		if (randomSelect.entries().size() > 1 || randomSelect.emptyWeight() > 0) {
			var chance = CommonProxy.chance(randomSelect.entries().get(index).weight() / (float) randomSelect.totalWeight());
			if (randomSelect.rolls() == BoundsExtensions.ONE) {
				list.add(Component.translatable("tip.lychee.randomChance.one", chance).withStyle(ChatFormatting.YELLOW));
			} else {
				list.add(Component.translatable(
								"tip.lychee.randomChance",
								chance,
								BoundsExtensions.getPlainDescription(randomSelect.rolls()))
						.withStyle(ChatFormatting.YELLOW));
			}
		}
		var c = randomSelect.conditions().showingCount() + child.conditions().showingCount();
		if (c > 0) {
			list.add(ClientProxy.format("contextual.lychee", c).withStyle(ChatFormatting.GRAY));
		}
		var mc = Minecraft.getInstance();
		randomSelect.conditions().appendToTooltips(list, mc.level, mc.player, 0);
		child.conditions().appendToTooltips(list, mc.level, mc.player, 0);
		return list;
	}

	default void internalRender(T action, GuiGraphicsExtractor graphics, int x, int y) {
		if (!action.hidden()) {
			Identifier sprite = action.commonProperties().icon();
			if (sprite != null) {
				graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 16, 16);
				return;
			}
		}
		render(action, graphics, x, y);
	}

	default void render(T action, GuiGraphicsExtractor graphics, int x, int y) {
	}

	default List<Component> getBaseTooltips(T action, @Nullable Player player) {
		return Lists.newArrayList(action.getDisplayName());
	}

	default List<Component> getTooltips(T action, @Nullable Player player) {
		var list = Lists.newArrayList(getBaseTooltips(action, player));
		appendConditionTooltips(list, action, player);
		return list;
	}

	default void loadCatalystsInfo(T action, ILycheeRecipe<?> recipe, List<IngredientInfo> ingredients) {}

	static void appendConditionTooltips(List<Component> tooltips, PostAction action, @Nullable Player player) {
		int c = action.conditions().showingCount();
		if (c > 0) {
			tooltips.add(ClientProxy.format("contextual.lychee", c).withStyle(ChatFormatting.GRAY));
		}
		var mc = Minecraft.getInstance();
		action.conditions().appendToTooltips(tooltips, mc.level, player, 0);
	}
}
