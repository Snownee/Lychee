package snownee.lychee.compat.rei.category;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
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
import snownee.lychee.action.DropItem;
import snownee.lychee.action.RandomSelect;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.compat.rei.LycheeREIPlugin;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.compat.rei.elements.InteractiveWidget;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.action.CompoundAction;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionRenderer;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public interface LycheeCategory<R extends ILycheeRecipe<LycheeContext>> {

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
				action.conditions().conditions().isEmpty() ? LycheeREIPlugin.SlotType.NORMAL : LycheeREIPlugin.SlotType.CHANCE);
		slot.markOutput();
		List<EntryStack<?>> entries = Lists.newArrayList();
		Map<EntryStack<ItemStack>, PostAction> itemMap = Maps.newHashMap();
		buildActionSlot(entries, action, itemMap);
		slot.entries(entries);
		widgets.add(slot);
		slot.addTooltipCallback(tooltip -> {
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
		if (action instanceof DropItem dropitem) {
			var entry = EntryStacks.of(dropitem.stack());
			entries.add(entry);
			itemMap.put(entry, dropitem);
		} else if (action instanceof CompoundAction compoundAction) {
			compoundAction.getChildActions().filter(it -> !it.hidden()).forEach(child -> buildActionSlot(
					entries,
					child,
					itemMap));
		} else {
			entries.add(EntryStack.of(LycheeREIPlugin.POST_ACTION, action));
		}
	}

	LycheeRecipeType<? extends R> recipeType();

	Rect2i infoRect();

	default int contentWidth() {
		return 120;
	}

	static void drawInfoBadgeIfNeeded(List<Widget> widgets, LycheeDisplay<?> display, Point startPoint, Rect2i rect) {
		ILycheeRecipe<?> recipe = display.recipe();
		if (recipe.conditions().conditions().isEmpty() && !recipe.comment().map(it -> !Strings.isNullOrEmpty(it)).orElse(false)) {
			return;
		}
		widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
			var matrixStack = graphics.pose();
			matrixStack.pushPose();
			matrixStack.translate(startPoint.x + rect.getX(), startPoint.y + rect.getY(), 0);
			matrixStack.scale(.5F, .5F, .5F);
			AllGuiTextures.INFO.render(graphics, 0, 0);
			matrixStack.popPose();
		}));
		var reactive = new InteractiveWidget(LycheeREIPlugin.offsetRect(startPoint, rect));
		reactive.setTooltipFunction($ -> JEIREI.getRecipeTooltip(recipe));
		reactive.setOnClick((widget, button) -> ClientProxy.postInfoBadgeClickEvent(
				display.recipe(),
				display.getDisplayLocation().orElse(null),
				button));
		widgets.add(reactive);
	}

	default void drawInfoBadgeIfNeeded(List<Widget> widgets, LycheeDisplay<R> display, Point startPoint) {
		drawInfoBadgeIfNeeded(widgets, display, startPoint, infoRect());
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
		var ingredients = JEIREI.generateShapelessInputs(recipe);
		slotGroup(
				widgets, startPoint, x, y, ingredients, (widgets0, startPoint0, ingredient, x0, y0) -> {
					var items = ingredient.ingredient.getItems();
					var slot = LycheeREIPlugin.slot(
							startPoint,
							x0,
							y0,
							ingredient.isCatalyst ? LycheeREIPlugin.SlotType.CATALYST : LycheeREIPlugin.SlotType.NORMAL);
					slot.entries(EntryIngredients.ofItemStacks(Stream.of(items)
							.map($ -> ingredient.count == 1 ? $ : $.copy())
							.peek($ -> $.setCount(ingredient.count))
							.toList()));
					slot.markInput();
					if (!ingredient.tooltips.isEmpty()) {
						slot.addTooltipCallback(tooltip -> {
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
