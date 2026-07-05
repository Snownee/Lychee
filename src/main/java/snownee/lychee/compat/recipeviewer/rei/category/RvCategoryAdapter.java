package snownee.lychee.compat.recipeviewer.rei.category;

import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.jspecify.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
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
import snownee.lychee.action.RandomSelect;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.compat.recipeviewer.SlotType;
import snownee.lychee.compat.recipeviewer.category.RvCategoryInstance;
import snownee.lychee.compat.recipeviewer.category.RvCategoryLayoutBuilder;
import snownee.lychee.compat.recipeviewer.category.RvCategoryWidgetBuilder;
import snownee.lychee.compat.recipeviewer.rei.LycheeREIClientPlugin;
import snownee.lychee.compat.recipeviewer.rei.LycheeREIPlugin;
import snownee.lychee.compat.recipeviewer.rei.display.LycheeDisplay;
import snownee.lychee.compat.recipeviewer.rei.element.RenderElementAdapter;
import snownee.lychee.compat.recipeviewer.rei.ingredient.PostActionIngredientRenderer;
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

	private static <T> void slotGroup(
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

	private static void actionSlot(
			ImmutableList.Builder<Widget> widgets,
			Vector2fc startPoint,
			PostAction action,
			int x,
			int y) {
		var slot = LycheeREIClientPlugin.slot(startPoint, x, y, action.conditions().showingCount() == 0 ? SlotType.NORMAL : SlotType.CHANCE);
		slot.markOutput();
		List<EntryStack<?>> entries = Lists.newArrayList();
		buildActionSlot(entries, action, action);
		slot.entries(entries);
		widgets.add(slot);
	}

	private static void buildActionSlot(List<EntryStack<?>> entries, PostAction rootAction, PostAction action) {
		switch (action) {
			case DropItem dropItem -> {
				EntryStack<ItemStack> itemEntry = EntryStacks.of(dropItem.itemStack().create());
				if (action.commonProperties().icon() != null || action.commonProperties().customName() != null ||
						action.commonProperties().conditions().hasShowingConditions()) {
					EntryStack<PostAction> actionEntry = EntryStack.of(LycheeREIPlugin.POST_ACTION, rootAction);
					EntryRenderer<PostAction> actionRenderer = actionEntry.getRenderer();
					itemEntry.withRenderer(new EntryRenderer<>() {
						@Override
						public void render(EntryStack<ItemStack> entry, GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
							actionRenderer.render(actionEntry, graphics, bounds, mouseX, mouseY, delta);
						}

						@Override
						public @Nullable Tooltip getTooltip(EntryStack<ItemStack> entry, me.shedaniel.rei.api.client.gui.widgets.TooltipContext context) {
							return null;
						}
					});
				}
				itemEntry.tooltipProcessor((entry, tooltip) -> actionTooltip(rootAction, action, tooltip));
				entries.add(itemEntry);
			}
			case CompoundAction compoundAction ->
					compoundAction.getChildActions().filter(it -> !it.hidden()).forEach(child -> buildActionSlot(entries, rootAction, child));
			default -> entries.add(EntryStack.of(LycheeREIPlugin.POST_ACTION, action));
		}
	}

	private static Tooltip actionTooltip(PostAction rootAction, PostAction action, Tooltip tooltip) {
		tooltip.entries().clear();
		List<Component> list;
		var player = Minecraft.getInstance().player;
		if (rootAction instanceof RandomSelect randomSelect) {
			list = ActionRenderer.getTooltipsFromRandom(randomSelect, action, player);
		} else {
			list = ActionRenderer.of(rootAction).getTooltips(rootAction, player);
		}
		tooltip.entries().addAll(list.stream().map(Tooltip::entry).toList());
		return tooltip;
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
		slotGroup(widgets, startPoint, x, y, recipe.postActions().stream().filter(it -> !it.hidden()).toList(), RvCategoryAdapter::actionSlot);
	}

	private void ingredientGroup(ImmutableList.Builder<Widget> widgets, Vector2fc startPoint, R recipe, float x, float y) {
		var ingredients = RVs.generateShapelessInputs(recipe);
		slotGroup(
				widgets, startPoint, x, y, ingredients, (widgets0, startPoint0, ingredient, x0, y0) -> {
					var slot = LycheeREIClientPlugin.slot(startPoint, x0, y0, ingredient.type);
					if (ingredient.ingredient.isEmpty()) {
						if (!ingredient.tooltips.isEmpty()) {
							slot.entry(EntryStack.of(LycheeREIPlugin.POST_ACTION, PostActionIngredientRenderer.INGREDIENT_HACK_DUMMY));
						}
					} else {
						slot.entries(EntryIngredients.ofSlotDisplay(ingredient.display()));
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
		var recipeHolder = display instanceof snownee.lychee.compat.recipeviewer.rei.display.SimpleLycheeDisplay<R> simple ? simple.recipeHolder() : null;
		if (recipeHolder == null) {
			return widgets.build();
		}
		var layoutBuilder = new RvCategoryLayoutBuilder.Wrapped<>(instance, recipeHolder) {
			@Override
			protected void _actionGroup(R recipe, Vector2fc position) {
				RvCategoryAdapter.this.actionGroup(widgets, startPoint, recipe, position.x(), position.y());
			}

			@Override
			protected void _ingredientGroup(R recipe, Vector2fc position) {
				RvCategoryAdapter.this.ingredientGroup(widgets, startPoint, recipe, position.x(), position.y());
			}
		};
		instance.type().configureLayout(layoutBuilder, recipeHolder);
		var widgetBuilder = new RvCategoryWidgetBuilder<>(instance, recipeHolder);
		instance.configureDecorations(widgetBuilder, recipeHolder);
		widgetBuilder.sortElements().map(it -> new RenderElementAdapter(it, startPoint)).forEach(widgets::add);
		return widgets.build();
	}

	@FunctionalInterface
	interface SlotLayoutFunction<T> {
		void apply(ImmutableList.Builder<Widget> widgets, Vector2fc startPoint, T item, int x, int y);
	}
}
