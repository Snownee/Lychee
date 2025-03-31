package snownee.lychee.compat.recipeviewer.rei.category;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.action.DropItem;
import snownee.lychee.action.PlaceBlock;
import snownee.lychee.action.RandomSelect;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.compat.recipeviewer.SlotType;
import snownee.lychee.compat.recipeviewer.category.RvCategory;
import snownee.lychee.compat.recipeviewer.category.RvCategoryLayoutBuilder;
import snownee.lychee.compat.recipeviewer.category.RvCategoryWidgetBuilder;
import snownee.lychee.compat.recipeviewer.rei.LycheeREIPlugin;
import snownee.lychee.compat.recipeviewer.rei.display.LycheeDisplay;
import snownee.lychee.compat.recipeviewer.rei.elements.RenderElementAdapter;
import snownee.lychee.util.action.CompoundAction;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionRenderer;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class RVCategoryAdapter<R extends ILycheeRecipe<LycheeContext>> implements DisplayCategory<LycheeDisplay<R>> {

	private final RvCategory<R> rvCategory;
	private final CategoryIdentifier<LycheeDisplay<R>> categoryIdentifier;
	private final Renderer icon;

	public RVCategoryAdapter(RvCategory<R> rvCategory) {
		this.rvCategory = rvCategory;
		this.categoryIdentifier = CategoryIdentifier.of(rvCategory.id());
		this.icon = new RenderElementAdapter(rvCategory.icon());
	}

	static <T> void slotGroup(
			ImmutableList.Builder<Widget> widgets,
			Vector2fc startPoint,
			float x,
			float y,
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
				layoutFunction.apply(widgets, startPoint, items.get(index), (int) (x + j * 19), (int) (y + i * 19));
				++index;
			}
		}
	}

	static void actionSlot(ImmutableList.Builder<Widget> widgets, Vector2fc startPoint, PostAction action, int x, int y) {
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

	@Override
	public CategoryIdentifier<? extends LycheeDisplay<R>> getCategoryIdentifier() {
		return categoryIdentifier;
	}

	@Override
	public Component getTitle() {
		return rvCategory.title();
	}

	@Override
	public Renderer getIcon() {
		return icon;
	}

	@Override
	public int getDisplayWidth(LycheeDisplay<R> display) {
		return rvCategory.width() + 10;
	}

	@Override
	public int getDisplayHeight() {
		return rvCategory.height() + 6;
	}

	private void actionGroup(ImmutableList.Builder<Widget> widgets, Vector2fc startPoint, R recipe, float x, float y) {
		slotGroup(
				widgets,
				startPoint,
				x,
				y,
				recipe.postActions().stream().filter(it -> !it.hidden()).toList(),
				RVCategoryAdapter::actionSlot);
	}

	private void ingredientGroup(ImmutableList.Builder<Widget> widgets, Vector2fc startPoint, R recipe, float x, float y) {
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

	@Override
	public List<Widget> setupDisplay(LycheeDisplay<R> display, Rectangle bounds) {
		var widgets = ImmutableList.<Widget>builder();
		var startPoint = new Vector2f(bounds.getCenterX() - (float) rvCategory.width() / 2, bounds.getCenterY() - (float) rvCategory.height() / 2 + 1);
		widgets.add(Widgets.createRecipeBase(bounds));

		var layoutBuilder = new RvCategoryLayoutBuilder() {
			@Override
			public void actionGroup(ILycheeRecipe<?> recipe, Vector2fc position) {
				RVCategoryAdapter.this.actionGroup(widgets, startPoint, (R) recipe, position.x(), position.y());
			}

			@Override
			public void ingredientGroup(ILycheeRecipe<?> recipe, Vector2fc position) {
				RVCategoryAdapter.this.ingredientGroup(widgets, startPoint, (R) recipe, position.x(), position.y());
			}
		};
		rvCategory.configureLayout(layoutBuilder, display.recipe(), startPoint);

		var widgetBuilder = new RvCategoryWidgetBuilder() {
			@Override
			public void addElement(RenderElement element) {
				var adapter = new RenderElementAdapter(element);
				widgets.add(adapter);
			}
		};
		rvCategory.configureDecorations(widgetBuilder, display.recipe(), startPoint);

		return widgets.build();
	}

	@FunctionalInterface
	interface SlotLayoutFunction<T> {
		void apply(ImmutableList.Builder<Widget> widgets, Vector2fc startPoint, T item, int x, int y);
	}
}
