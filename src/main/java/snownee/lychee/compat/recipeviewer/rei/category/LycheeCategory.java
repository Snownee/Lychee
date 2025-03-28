package snownee.lychee.compat.recipeviewer.rei.category;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.Lychee;
import snownee.lychee.action.DropItem;
import snownee.lychee.action.PlaceBlock;
import snownee.lychee.action.RandomSelect;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.compat.recipeviewer.RvCategoryProvider;
import snownee.lychee.compat.recipeviewer.SlotType;
import snownee.lychee.compat.recipeviewer.rei.LycheeREIPlugin;
import snownee.lychee.compat.recipeviewer.rei.display.LycheeDisplay;
import snownee.lychee.compat.recipeviewer.rei.elements.InteractiveWidget;
import snownee.lychee.ui.SpriteElementRenderer;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.action.CompoundAction;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionRenderer;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;

public interface LycheeCategory<R extends ILycheeRecipe<LycheeContext>> extends RvCategoryProvider<R> {

	static <T> void slotGroup(
			List<Widget> widgets,
			Point startPoint,
			int x,
			int y,
			List<T> items,
			SlotLayoutFunction<T> layoutFunction
	) {
		var size = Math.min(items.size(), 9);
		var gridX = (int) Math.ceil(Math.sqrt(size));
		var gridY = (int) Math.ceil((float) size / gridX);

		x -= gridX * 9;
		y -= gridY * 9;

		var index = 0;
		for (var i = 0; i < gridY; i++) {
			for (var j = 0; j < gridX; j++) {
				if (index >= size) {
					break;
				}
				layoutFunction.apply(widgets, startPoint, items.get(index), x + j * 19, y + i * 19);
				++index;
			}
		}
	}

	static void actionSlot(List<Widget> widgets, Point startPoint, PostAction action, int x, int y) {
		var slot = LycheeREIPlugin.slot(
				startPoint,
				x,
				y,
				action.conditions().conditions().isEmpty() ? SlotType.NORMAL : SlotType.CHANCE);
		slot.markOutput();
		List<EntryStack<?>> entries = Lists.newArrayList();
		Map<EntryStack<ItemStack>, PostAction> itemMap = Maps.newHashMap();
		buildActionSlot(entries, action, itemMap);
		slot.entries(entries);
		widgets.add(slot);
		slot.tooltipProcessor(tooltip -> {
			if (tooltip == null) {
				return null;
			}
			Object raw = tooltip.getContextStack();
			if (!itemMap.containsKey(raw)) {
				//System.out.println(itemMap);
				return tooltip;
			}
			tooltip.entries().clear();
			raw = itemMap.get(raw);
			List<Component> list;
			var player = Minecraft.getInstance().player;
			if (action instanceof RandomSelect randomSelect) {
				list = PostActionRenderer.getTooltipsFromRandom(randomSelect, (PostAction) raw, player);
			} else {
				list = PostActionRenderer.of(action).getTooltips(action, player);
			}
			tooltip.entries().addAll(list.stream().map(Tooltip::entry).toList());
			return tooltip;
		});
	}

	static void buildActionSlot(
			List<EntryStack<?>> entries,
			PostAction action,
			Map<EntryStack<ItemStack>, PostAction> itemMap
	) {
		switch (action) {
			case DropItem dropitem -> {
				var entry = EntryStacks.of(dropitem.stack());
				entries.add(entry);
				itemMap.put(entry, dropitem);
			}
			case CompoundAction compoundAction ->
					compoundAction.getChildActions().filter(it -> !it.hidden()).forEach(child -> buildActionSlot(
							entries,
							child,
							itemMap));
			default -> {
				if (action instanceof PlaceBlock placeBlock && BlockPredicateExtensions.anyBlockState(placeBlock.block()).isAir()) {
					return;
				}
				entries.add(EntryStack.of(LycheeREIPlugin.POST_ACTION, action));
			}
		}
	}

	static <T extends ILycheeRecipe<LycheeContext>> void addRemoveInputBlock(
			int x,
			int y,
			List<Widget> widgets,
			T recipe) {
		if (recipe.postActions().stream().noneMatch(it -> it instanceof PlaceBlock placeBlock && placeBlock.hidden())) {
			return;
		}
		var widget = new InteractiveWidget(new Rectangle(x, y, 8, 8));
		widgets.add(widget);
		widget.setRenderable(new SpriteElementRenderer(
				Lychee.id("exclamation_mark"),
				new Rect2i(x, y, widget.getBounds().width, widget.getBounds().height),
				100,
				2));
		widget.setTooltipFunction(it -> List.of(Component.translatable("postAction.lychee.place.consume")));
	}

	Rect2i infoRect();

	default int contentWidth() {
		return rvCategory().type.width;
	}

	static void createInfoBadgeIfNeeded(List<Widget> widgets, LycheeDisplay<?> display, Point startPoint, Rect2i rect) {
		var recipe = display.recipe().value();
		if (recipe.conditions().conditions().isEmpty() && !recipe.comment().map(it -> !Strings.isNullOrEmpty(it)).orElse(false)) {
			return;
		}
		Rectangle bounds = LycheeREIPlugin.offsetRect(startPoint, rect);
		var widget = new InteractiveWidget(bounds);
		widget.setTooltipFunction($ -> RVs.getRecipeTooltip(recipe));
		widget.setOnClick(($, button) -> ClientProxy.postInfoBadgeClickEvent(
				recipe,
				display.getDisplayLocation().orElse(null),
				button));
		widget.setRenderable((graphics, mouseX, mouseY, delta) -> {
			var matrixStack = graphics.pose();
			matrixStack.pushPose();
			matrixStack.translate(bounds.x, bounds.y, 0);
			matrixStack.scale(.5F, .5F, .5F);
			AllGuiTextures.INFO.render(graphics, 0, 0);
			matrixStack.popPose();
		});
		widgets.add(widget);
	}

	default void createInfoBadgeIfNeeded(List<Widget> widgets, LycheeDisplay<R> display, Point startPoint) {
		createInfoBadgeIfNeeded(widgets, display, startPoint, infoRect());
	}

	default void actionGroup(List<Widget> widgets, Point startPoint, R recipe, int x, int y) {
		slotGroup(
				widgets,
				startPoint,
				x,
				y,
				recipe.postActions().stream().filter(it -> !it.hidden()).toList(),
				LycheeCategory::actionSlot);
	}

	default void ingredientGroup(List<Widget> widgets, Point startPoint, R recipe, int x, int y) {
		var ingredients = RVs.generateShapelessInputs(recipe);
		slotGroup(
				widgets, startPoint, x, y, ingredients, (widgets0, startPoint0, ingredient, x0, y0) -> {
					var items = ingredient.ingredient.getItems();
					var slot = LycheeREIPlugin.slot(startPoint, x0, y0, ingredient.type);
					slot.entries(EntryIngredients.ofItemStacks(Stream.of(items)
							.map($ -> ingredient.count == 1 ? $ : $.copy())
							.peek($ -> $.setCount(ingredient.count))
							.toList()));
					slot.markInput();
					if (!ingredient.tooltips.isEmpty()) {
						slot.tooltipProcessor(tooltip -> {
							if (tooltip == null) {
								tooltip = Tooltip.create();
							}
							ingredient.tooltips.forEach(tooltip::add);
							return tooltip;
						});
					}
					widgets.add(slot);
				});
	}

	default boolean clickBlock(BlockState state, int button) {
		if (state.is(Blocks.CHIPPED_ANVIL) || state.is(Blocks.DAMAGED_ANVIL)) {
			state = Blocks.ANVIL.defaultBlockState();
		}
		var stack = state.getBlock().asItem().getDefaultInstance();
		EntryStack<?> entry;
		if (!stack.isEmpty()) {
			entry = EntryStacks.of(stack);
		} else if (state.getBlock() instanceof LiquidBlock) {
			entry = EntryStacks.of(state.getFluidState().getType());
		} else {
			return false;
		}
		var searchBuilder = ViewSearchBuilder.builder();
		if (button == 0) {
			searchBuilder.addRecipesFor(entry);
		} else if (button == 1) {
			searchBuilder.addUsagesFor(entry);
		} else {
			return false;
		}
		searchBuilder.open();
		return true;
	}

	@FunctionalInterface
	interface SlotLayoutFunction<T> {
		void apply(List<Widget> widgets, Point startPoint, T item, int x, int y);
	}
}
