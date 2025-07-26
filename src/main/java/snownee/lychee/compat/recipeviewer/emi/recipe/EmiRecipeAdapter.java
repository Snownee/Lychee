package snownee.lychee.compat.recipeviewer.emi.recipe;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joml.Vector2fc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.ItemEmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import snownee.lychee.action.DropItem;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.compat.recipeviewer.SlotType;
import snownee.lychee.compat.recipeviewer.category.RvCategoryInstance;
import snownee.lychee.compat.recipeviewer.category.RvCategoryLayoutBuilder;
import snownee.lychee.compat.recipeviewer.category.RvCategoryWidgetBuilder;
import snownee.lychee.compat.recipeviewer.emi.category.RvCategoryAdapter;
import snownee.lychee.compat.recipeviewer.emi.element.EmiWidgetAdapter;
import snownee.lychee.compat.recipeviewer.emi.element.LycheeSlotWidget;
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

	public EmiRecipeAdapter(RvCategoryAdapter<R> category, RecipeHolder<R> recipe) {
		this.category = category;
		this.recipe = recipe;
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
		R recipe = this.recipe.value();
		List<EmiIngredient> ingredients = Lists.newArrayList();
		try {
			recipe.sizedIngredients().stream()
					.map($ -> EmiIngredient.of($.ingredient(), $.count()))
					.forEach(ingredients::add);
		} catch (UnsupportedOperationException ignored) {
			recipe.getIngredients().stream()
					.map(EmiIngredient::of)
					.forEach(ingredients::add);
		}
		recipe.getBlockInputs().stream()
				.map(BlockPredicateExtensions::matchedFluids)
				.flatMap(Set::stream)
				.distinct()
				.map(EmiStack::of)
				.forEach(ingredients::add);
		recipe.getBlockInputs().stream()
				.map(BlockPredicateExtensions::matchedBlocks)
				.flatMap(Set::stream)
				.map(Block::asItem)
				.filter(it -> !it.equals(Items.AIR))
				.distinct()
				.map(EmiStack::of)
				.forEach(ingredients::add);
		return ingredients;
	}

	@Override
	public List<EmiIngredient> getCatalysts() {
		return EmiRecipe.super.getCatalysts();
	}

	@Override
	public List<EmiStack> getOutputs() {
		R recipe = this.recipe.value();
		List<EmiStack> ingredients = Lists.newArrayList();
		recipe.allActions().filter(it -> !it.hidden())
				.map(PostActionDisplay::getOutputItems)
				.flatMap(List::stream)
				.map(EmiStack::of)
				.forEach(ingredients::add);
		recipe.getBlockOutputs().stream()
				.map(BlockPredicateExtensions::matchedFluids)
				.flatMap(Set::stream)
				.distinct()
				.map(EmiStack::of)
				.forEach(ingredients::add);
		return ingredients;
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
			protected void _ingredientGroup(R recipe, Vector2fc position) {
				EmiRecipeAdapter.this.ingredientGroup(widgets, recipe, position.x(), position.y());
			}
		};
		instance.type().configureLayout(layoutBuilder, recipe);

		var widgetBuilder = new RvCategoryWidgetBuilder<>(instance, recipe) {
			@Override
			public void addElement(RenderElement element) {
				widgets.add(new EmiWidgetAdapter(element));
			}
		};
		instance.configureDecorations(widgetBuilder, recipe);
	}

	private void ingredientGroup(WidgetHolder widgets, R recipe, float x, float y) {
		var ingredients = RVs.generateShapelessInputs(recipe);
	}

	private void actionGroup(WidgetHolder widgets, R recipe, float x, float y) {
		slotGroup(widgets, x, y, recipe.postActions().stream().filter(it -> !it.hidden()).toList(), this::actionSlot);
	}

	static <T> void slotGroup(
			WidgetHolder widgets,
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
//		widget.appendTooltip()
	}

	private void buildActionSlot(
			List<EmiIngredient> entries,
			PostAction action,
			Map<EmiIngredient, PostAction> itemMap) {
		switch (action) {
			case DropItem dropItem -> {
				ActionRenderer<PostAction> renderer = ActionRenderer.of(action);
				ItemEmiStack entry = new ItemEmiStack(dropItem.itemStack()) {
					@Override
					public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
						super.render(draw, x, y, delta, flags);
					}
				};
				entries.add(entry);
				itemMap.put(entry, dropItem);
			}
			case CompoundAction compoundAction -> {
				compoundAction.getChildActions().filter(it -> !it.hidden()).forEach(child -> buildActionSlot(
						entries,
						child,
						itemMap));
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
