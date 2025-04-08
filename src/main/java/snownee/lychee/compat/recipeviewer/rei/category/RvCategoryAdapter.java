package snownee.lychee.compat.recipeviewer.rei.category;

import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2fc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.action.DropItem;
import snownee.lychee.action.RandomSelect;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.client.gui.WrapperRenderElement;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.compat.recipeviewer.SlotType;
import snownee.lychee.compat.recipeviewer.category.RvCategoryInstance;
import snownee.lychee.compat.recipeviewer.category.RvCategoryLayoutBuilder;
import snownee.lychee.compat.recipeviewer.category.RvCategoryWidgetBuilder;
import snownee.lychee.compat.recipeviewer.rei.LycheeREIPlugin;
import snownee.lychee.compat.recipeviewer.rei.display.LycheeDisplay;
import snownee.lychee.compat.recipeviewer.rei.element.RenderElementAdapter;
import snownee.lychee.ui.TextElementRenderer;
import snownee.lychee.util.action.ActionRenderer;
import snownee.lychee.util.action.CompoundAction;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class RvCategoryAdapter<R extends ILycheeRecipe<LycheeContext>> implements DisplayCategory<LycheeDisplay<R>> {

	private final RvCategoryInstance<R> instance;
	private final CategoryIdentifier<LycheeDisplay<R>> categoryIdentifier;
	private final Renderer icon;

	public RvCategoryAdapter(RvCategoryInstance<R> instance) {
		this.instance = instance;
		this.categoryIdentifier = CategoryIdentifier.of(instance.id());
		this.icon = new RenderElementAdapter(instance.icon());
	}

	static <T> void slotGroup(
			ImmutableList.Builder<Widget> widgets,
			Vector2fc startPoint,
			float x,
			float y,
			List<T> items,
			SlotLayoutFunction<T> layoutFunction) {
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
		var slot = LycheeREIPlugin.slot(startPoint, x, y, action.conditions().conditions().isEmpty() ? SlotType.NORMAL : SlotType.CHANCE);
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
				list = ActionRenderer.getTooltipsFromRandom(randomSelect, (PostAction) raw, player);
			} else {
				list = ActionRenderer.of(action).getTooltips(action, player);
			}
			tooltip.entries().addAll(list.stream().map(Tooltip::entry).toList());
			return tooltip;
		});
	}

	static void buildActionSlot(List<EntryStack<?>> entries, PostAction action, Map<EntryStack<ItemStack>, PostAction> itemMap) {
		EntryStack<PostAction> entry = EntryStack.of(LycheeREIPlugin.POST_ACTION, action);
		switch (action) {
			case DropItem dropitem -> {
				var itemEntry = EntryStacks.of(dropitem.itemStack());
				if (action.commonProperties().icon() != null) {
					var originalRenderer = itemEntry.getRenderer();
					itemEntry.withRenderer(new EntryRenderer<>() {
						@Override
						public void render(
								EntryStack<ItemStack> entryStack,
								GuiGraphics guiGraphics,
								Rectangle rectangle,
								int mx,
								int my,
								float delta) {
							entry.getRenderer().render(entry, guiGraphics, rectangle, mx, my, delta);
						}

						@Override
						public @Nullable Tooltip getTooltip(EntryStack<ItemStack> entryStack, TooltipContext tooltipContext) {
							return originalRenderer.getTooltip(entryStack, tooltipContext);
						}
					});
				}
				entries.add(itemEntry);
				itemMap.put(itemEntry, dropitem);
			}
			case CompoundAction compoundAction ->
					compoundAction.getChildActions().filter(it -> !it.hidden()).forEach(child -> buildActionSlot(entries, child, itemMap));
			default -> entries.add(entry);
		}
	}

	@Override
	public CategoryIdentifier<? extends LycheeDisplay<R>> getCategoryIdentifier() {
		return categoryIdentifier;
	}

	@Override
	public Component getTitle() {
		return instance.title();
	}

	@Override
	public Renderer getIcon() {
		return icon;
	}

	@Override
	public int getDisplayWidth(LycheeDisplay<R> display) {
		return instance.width() + 10;
	}

	@Override
	public int getDisplayHeight() {
		return instance.height() + 6;
	}

	private void actionGroup(ImmutableList.Builder<Widget> widgets, Vector2fc startPoint, R recipe, float x, float y) {
		slotGroup(
				widgets,
				startPoint,
				x,
				y,
				recipe.postActions().stream().filter(it -> !it.hidden()).toList(),
				RvCategoryAdapter::actionSlot);
	}

	private void ingredientGroup(ImmutableList.Builder<Widget> widgets, Vector2fc startPoint, R recipe, float x, float y) {
		var ingredients = RVs.generateShapelessInputs(recipe);
		slotGroup(
				widgets, startPoint, x, y, ingredients, (widgets0, startPoint0, ingredient, x0, y0) -> {
					var slot = LycheeREIPlugin.slot(startPoint, x0, y0, ingredient.type);
					if (ingredient.count == 1) {
						slot.entries(EntryIngredients.ofIngredient(ingredient.ingredient));
					} else {
						slot.entries(EntryIngredients.ofItemStacks(Stream.of(ingredient.ingredient.getItems())
								.map($ -> $.copyWithCount(ingredient.count))
								.toList()));
					}
					slot.markInput();
					slot.setExtraTooltips(ingredient.tooltips);
					widgets.add(slot);
				});
	}

	@Override
	public List<Widget> setupDisplay(LycheeDisplay<R> display, Rectangle bounds) {
		var widgets = ImmutableList.<Widget>builder();
		var startPoint = new Vector2f(
				bounds.getCenterX() - (float) instance.width() / 2,
				bounds.getCenterY() - (float) instance.height() / 2 + 1);
		widgets.add(Widgets.createRecipeBase(bounds));

		var layoutBuilder = new RvCategoryLayoutBuilder.Wrapped<>(instance, display.recipe()) {
			@Override
			protected void _actionGroup(R recipe, Vector2fc position) {
				RvCategoryAdapter.this.actionGroup(widgets, startPoint, recipe, position.x(), position.y());
			}

			@Override
			protected void _ingredientGroup(R recipe, Vector2fc position) {
				RvCategoryAdapter.this.ingredientGroup(widgets, startPoint, recipe, position.x(), position.y());
			}
		};
		instance.type().configureLayout(layoutBuilder, display.recipe());

		var widgetBuilder = new RvCategoryWidgetBuilder<>(instance, display.recipe()) {
			@Override
			public void addElement(RenderElement element) {
				ScreenElement unwrapped = WrapperRenderElement.unwrap(element);
				if (unwrapped instanceof TextElementRenderer text) {
					text.offset(startPoint);
					Point point = new Point(element.x(), element.y());
					var widget = Widgets.createLabel(point, REIRuntime.getInstance().isDarkThemeEnabled() ? text.darkText : text.text);
					if (text.text != text.darkText) {
						widget.setOnRender((graphics, label) -> {
							Component msg = REIRuntime.getInstance().isDarkThemeEnabled() ? text.darkText : text.text;
							if (msg != label.getMessage()) {
								label.setMessage(msg);
							}
						});
					}
					widget.shadow(text.shadow);
					widget.color(text.lightModeColor, text.darkModeColor);
					if (text.centered) {
						widget.centered();
					}
					if (element instanceof InteractiveRenderElement interactive) {
						widget.setTooltipFunction(it -> {
							List<Component> tooltip = interactive.getTooltip();
							if (tooltip == null) {
								return null;
							}
							return tooltip.toArray(Component[]::new);
						});
						IntPredicate onClick = interactive.getOnClick();
						if (onClick != null) {
							widget.clickable();
							widget.setOnClick(it -> onClick.test(0));
						}
					}
					widgets.add(widget);
					return;
				}
				widgets.add(new RenderElementAdapter(element, startPoint));
			}
		};
		instance.configureDecorations(widgetBuilder, display.recipe());
		return widgets.build();
	}

	@FunctionalInterface
	interface SlotLayoutFunction<T> {
		void apply(ImmutableList.Builder<Widget> widgets, Vector2fc startPoint, T item, int x, int y);
	}
}
