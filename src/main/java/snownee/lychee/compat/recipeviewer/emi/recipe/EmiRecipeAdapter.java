package snownee.lychee.compat.recipeviewer.emi.recipe;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joml.Vector2fc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import snownee.lychee.RecipeTypes;
import snownee.lychee.action.DropItem;
import snownee.lychee.action.input.DamageItem;
import snownee.lychee.action.input.PreventDefault;
import snownee.lychee.client.gui.InteractiveRenderElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.client.gui.WrapperRenderElement;
import snownee.lychee.compat.recipeviewer.IngredientInfo;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.compat.recipeviewer.SlotType;
import snownee.lychee.compat.recipeviewer.category.RvCategory;
import snownee.lychee.compat.recipeviewer.category.RvCategoryInstance;
import snownee.lychee.compat.recipeviewer.category.RvCategoryLayoutBuilder;
import snownee.lychee.compat.recipeviewer.category.RvCategoryLayoutBuilder.IngredientLayout;
import snownee.lychee.compat.recipeviewer.category.RvCategoryWidgetBuilder;
import snownee.lychee.compat.recipeviewer.emi.category.RvCategoryAdapter;
import snownee.lychee.compat.recipeviewer.emi.element.EmiWidgetAdapter;
import snownee.lychee.compat.recipeviewer.emi.element.LycheeSlotWidget;
import snownee.lychee.compat.recipeviewer.emi.ingredient.PostActionEmiStack;
import snownee.lychee.ui.TextElementRenderer;
import snownee.lychee.util.action.ActionRenderer;
import snownee.lychee.util.action.CompoundAction;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionDisplay;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class EmiRecipeAdapter<R extends ILycheeRecipe<LycheeContext>> implements EmiRecipe {
	private final RvCategoryAdapter<R> category;
	private final RecipeHolder<R> recipe;
	protected List<EmiIngredient> inputs = Lists.newArrayList();
	protected List<Pair<IngredientInfo, EmiIngredient>> ingredients = Lists.newArrayList();
	protected List<EmiIngredient> catalysts = Lists.newArrayList();
	protected List<EmiStack> outputs = Lists.newArrayList();

	public EmiRecipeAdapter(RvCategoryAdapter<R> category, RecipeHolder<R> recipeHolder) {
		this.category = category;
		this.recipe = recipeHolder;

		R recipe = recipeHolder.value();
		List<IngredientInfo> ingredients = RVs.generateShapelessInputs(recipe);
		for (IngredientInfo ingredient : ingredients) {
			EmiIngredient emiIngredient = EmiIngredient.of(ingredient.ingredient, ingredient.count);
			if (ingredient.relatedAction != null) {
				for (EmiStack emiStack : emiIngredient.getEmiStacks()) {
					ItemStack itemStack = emiStack.getItemStack();
					if (itemStack.isEmpty()) {
						continue;
					}
					itemStack = ingredient.relatedAction.transformRemainder(itemStack, recipe);
					if (!itemStack.isEmpty()) {
						emiStack.setRemainder(EmiStack.of(itemStack));
					}
				}
			}
			this.ingredients.add(Pair.of(ingredient, emiIngredient));
			if (ingredient.type == SlotType.CATALYST && ingredient.relatedAction instanceof PreventDefault) {
				catalysts.add(emiIngredient);
			} else {
				inputs.add(emiIngredient);
			}
		}

		//TODO we need better handling of block inputs
		List<EmiIngredient> list;
		if (recipe.getType() == RecipeTypes.BLOCK_EXPLODING) {
			list = inputs;
		} else {
			list = RvCategory.needConsumeBlockInput(recipe) ? inputs : catalysts;
		}
		recipe.getBlockInputs()
				.stream()
				.map(BlockPredicateExtensions::matchedFluids)
				.flatMap(Set::stream)
				.distinct()
				.map(EmiStack::of)
				.forEach(list::add);
		recipe.getBlockInputs()
				.stream()
				.map(BlockPredicateExtensions::matchedBlocks)
				.flatMap(Set::stream)
				.map(Block::asItem)
				.filter(it -> !it.equals(Items.AIR))
				.distinct()
				.map(EmiStack::of)
				.forEach(list::add);

		recipe.allActions()
				.filter(it -> !it.hidden())
				.map(PostActionDisplay::getOutputItems)
				.flatMap(List::stream)
				.map(EmiStack::of)
				.forEach(outputs::add);
		recipe.getBlockOutputs()
				.stream()
				.map(BlockPredicateExtensions::matchedFluids)
				.flatMap(Set::stream)
				.distinct()
				.map(EmiStack::of)
				.forEach(outputs::add);
	}

	@Override
	public EmiRecipeCategory getCategory() {
		return category;
	}

	@Override
	public ResourceLocation getId() {
		return recipe.id();
	}

	@Override
	public List<EmiIngredient> getInputs() {
		return inputs;
	}

	@Override
	public List<EmiIngredient> getCatalysts() {
		return catalysts;
	}

	@Override
	public List<EmiStack> getOutputs() {
		return outputs;
	}

	@Override
	public int getDisplayWidth() {
		return category.instance().width();
	}

	@Override
	public int getDisplayHeight() {
		return category.instance().height();
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		RvCategoryInstance<R> instance = category.instance();
		var layoutBuilder = new RvCategoryLayoutBuilder.Wrapped<>(instance, recipe) {
			@Override
			protected void _actionGroup(R recipe, Vector2fc position) {
				EmiRecipeAdapter.this.actionGroup(widgets, recipe, position.x(), position.y());
			}

			@Override
			protected void _ingredientGroup(R recipe, IngredientLayout layout) {
				EmiRecipeAdapter.this.ingredientGroup(widgets, recipe, layout);
			}
		};
		instance.type().configureLayout(layoutBuilder, recipe);

		var widgetBuilder = new RvCategoryWidgetBuilder<>(instance, recipe) {
			@Override
			public void addElement(RenderElement element) {
				boolean interactive = false;
				if (element instanceof InteractiveRenderElement interactiveElement) {
					interactive = interactiveElement.getTooltip() != null || interactiveElement.getOnInput() != null;
				}
				if (!interactive) {
					ScreenElement unwrapped = WrapperRenderElement.unwrap(element);
					if (unwrapped instanceof TextElementRenderer text) {
						int x = (int) element.x();
						if (text.centered) {
							x -= Minecraft.getInstance().font.width(text.text) / 2;
						}
						widgets.addText(text.text, x, (int) element.y(), text.lightModeColor, text.shadow);
						return;
					}
				}
				widgets.add(new EmiWidgetAdapter(element));
			}
		};
		instance.configureDecorations(widgetBuilder, recipe);
	}

	private void ingredientGroup(WidgetHolder widgets, R recipe, IngredientLayout layout) {
		var size = Math.min(ingredients.size(), 9);
		for (var index = 0; index < size; index++) {
			var position = layout.position(index, size);
			var pair = ingredients.get(index);
			IngredientInfo info = pair.getFirst();
			LycheeSlotWidget widget = widgets.add(new LycheeSlotWidget(pair.getSecond(), (int) position.x(), (int) position.y(), info.type));
			if (!(info.relatedAction instanceof DamageItem)) {
				for (Component tooltip : info.tooltips) {
					widget.appendTooltip(tooltip);
				}
			}
		}
	}

	private void actionGroup(WidgetHolder widgets, R recipe, float x, float y) {
		slotGroup(widgets, x, y, recipe.postActions().stream().filter(it -> !it.hidden()).toList(), this::actionSlot);
	}

	static <T> void slotGroup(WidgetHolder widgets, float x, float y, List<T> items, SlotLayoutFunction<T> layoutFunction) {
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
				layoutFunction.apply(widgets, items.get(index), (int) (x + j * 19), (int) (y + i * 19));
				++index;
			}
		}
	}

	private void actionSlot(WidgetHolder widgets, PostAction action, float x, float y) {
		List<EmiIngredient> entries = Lists.newArrayList();
		Map<EmiIngredient, PostAction> itemMap = Maps.newHashMap();
		buildActionSlot(entries, action, itemMap);
		SlotType slotType = action.conditions().showingCount() == 0 ? SlotType.NORMAL : SlotType.CHANCE;
		LycheeSlotWidget widget = widgets.add(new LycheeSlotWidget(EmiIngredient.of(entries), (int) x, (int) y, slotType));
		widget.recipeContext(this);
	}

	@SuppressWarnings("UnstableApiUsage")
	private void buildActionSlot(List<EmiIngredient> entries, PostAction action, Map<EmiIngredient, PostAction> itemMap) {
		switch (action) {
			case DropItem dropItem -> {
				ActionRenderer<PostAction> renderer = ActionRenderer.of(action);
				ItemEmiStack entry = new ItemEmiStack(dropItem.itemStack()) {
					@Override
					public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
						if (action.commonProperties().icon() == null) {
							super.render(draw, x, y, delta, flags);
						} else {
							renderer.internalRender(action, draw, x, y);
						}
					}

					@Override
					public List<ClientTooltipComponent> getTooltip() {
						List<ClientTooltipComponent> tooltip = super.getTooltip();
						List<Component> list = Lists.newArrayList();
						ActionRenderer.appendConditionTooltips(list, action, Minecraft.getInstance().player);
						if (!list.isEmpty()) {
							list.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).forEach(tooltip::add);
						}
						return tooltip;
					}
				};
				if (action.commonProperties().icon() != null) {
					entry.setUnbatchable();
				}
				entries.add(entry);
				itemMap.put(entry, dropItem);
			}
			case CompoundAction compoundAction -> {
				compoundAction.getChildActions().filter(it -> !it.hidden()).forEach(child -> buildActionSlot(entries, child, itemMap));
			}
			default -> entries.add(new PostActionEmiStack(action));
		}
	}

	@Override
	public boolean hideCraftable() {
		return true;
	}

	@FunctionalInterface
	interface SlotLayoutFunction<T> {
		void apply(WidgetHolder widgets, T item, float x, float y);
	}
}
