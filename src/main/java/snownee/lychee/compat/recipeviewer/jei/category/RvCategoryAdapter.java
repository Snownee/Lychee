package snownee.lychee.compat.recipeviewer.jei.category;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2fc;

import com.google.common.collect.Maps;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluid;
import snownee.lychee.action.DropItem;
import snownee.lychee.action.RandomSelect;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.compat.recipeviewer.RVs;
import snownee.lychee.compat.recipeviewer.SlotType;
import snownee.lychee.compat.recipeviewer.category.RvCategoryInstance;
import snownee.lychee.compat.recipeviewer.category.RvCategoryLayoutBuilder;
import snownee.lychee.compat.recipeviewer.category.RvCategoryWidgetBuilder;
import snownee.lychee.compat.recipeviewer.jei.LycheeJEIPlugin;
import snownee.lychee.compat.recipeviewer.jei.element.RenderElementAdapter;
import snownee.lychee.compat.recipeviewer.jei.ingredient.PostActionIngredientRenderer;
import snownee.lychee.util.action.ActionRenderer;
import snownee.lychee.util.action.CompoundAction;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;


public class RvCategoryAdapter<R extends ILycheeRecipe<LycheeContext>> implements IRecipeCategory<RecipeHolder<R>> {
	private final RvCategoryInstance<R> instance;
	private final RecipeType<RecipeHolder<R>> type;
	private final IDrawable icon;

	public RvCategoryAdapter(RvCategoryInstance<R> instance) {
		this.instance = instance;
		this.type = RecipeType.createRecipeHolderType(instance.id());
		this.icon = new RenderElementAdapter(instance.icon());
	}

	private void addBlockIngredients(IRecipeLayoutBuilder builder, ILycheeRecipe<LycheeContext> recipe) {
		addBlockIngredients(builder, recipe.getBlockInputs(), RecipeIngredientRole.INPUT);
		addBlockIngredients(builder, recipe.getBlockOutputs(), RecipeIngredientRole.OUTPUT);
	}

	private void addBlockIngredients(IRecipeLayoutBuilder builder, Iterable<BlockPredicate> blocks, RecipeIngredientRole role) {
		for (BlockPredicate block : blocks) {
			List<ItemStack> items = BlockPredicateExtensions.matchedItemStacks(block);
			Set<Fluid> fluids = BlockPredicateExtensions.matchedFluids(block);
			if (!items.isEmpty() || !fluids.isEmpty()) {
				IIngredientAcceptor<?> acceptor = builder.addInvisibleIngredients(role);
				acceptor.addItemStacks(items);
				fluids.forEach(fluid -> acceptor.addFluidStack(
						fluid,
						((JeiRvHelper) instance.helper()).jeiHelpers().getPlatformFluidHelper().bucketVolume()));
			}
		}
	}

	private static <T> void slotGroup(IRecipeLayoutBuilder builder, float x, float y, List<T> items, SlotLayoutFunction<T> layoutFunction) {
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
				layoutFunction.apply(builder, items.get(index), x + j * 19, y + i * 19);
				++index;
			}
		}
	}

	private void actionSlot(IRecipeLayoutBuilder builder, PostAction action, float x, float y) {
		var slotBuilder = builder.addSlot(RecipeIngredientRole.OUTPUT, (int) (x + 1), (int) (y + 1));
		var itemMap = Maps.<ItemStack, PostAction>newIdentityHashMap();
		buildActionSlot(builder, slotBuilder, action, itemMap);
		slotBuilder.addRichTooltipCallback((view, tooltip) -> {
			var displayedIngredient = view.getDisplayedIngredient();
			if (displayedIngredient.isEmpty()) {
				return;
			}
			var raw = displayedIngredient.get().getIngredient();
			if (!itemMap.containsKey(raw)) {
				return;
			}
			tooltip.clear();
			raw = itemMap.get(raw);
			List<Component> list;
			var player = Minecraft.getInstance().player;
			if (action instanceof RandomSelect randomSelect) {
				list = ActionRenderer.getTooltipsFromRandom(randomSelect, (PostAction) raw, player);
			} else {
				list = ActionRenderer.of(action).getTooltips(action, player);
			}
			tooltip.addAll(list);
		});
		SlotType slotType = action.conditions().conditions().isEmpty() ? SlotType.NORMAL : SlotType.CHANCE;
		slotBuilder.setBackground(LycheeJEIPlugin.slot(slotType), -1, -1);
	}

	private void buildActionSlot(
			IRecipeLayoutBuilder builder,
			IRecipeSlotBuilder slotBuilder,
			PostAction action,
			Map<ItemStack, PostAction> itemMap) {
		switch (action) {
			case DropItem dropItem -> {
				slotBuilder.addItemStack(dropItem.itemStack());
				if (action.commonProperties().icon() != null) {
					slotBuilder.setCustomRenderer(
							VanillaTypes.ITEM_STACK, new IIngredientRenderer<>() {
								@Override
								public void render(GuiGraphics guiGraphics, ItemStack ingredient) {
									PostActionIngredientRenderer.INSTANCE.render(guiGraphics, action);
								}

								@Override
								public void getTooltip(ITooltipBuilder tooltip, ItemStack ingredient, TooltipFlag tooltipFlag) {
									IIngredientManager ingredientManager = ((JeiRvHelper) instance.helper()).jeiHelpers()
											.getIngredientManager();
									ingredientManager.getIngredientRenderer(ingredient).getTooltip(tooltip, ingredient, tooltipFlag);
								}

								@SuppressWarnings("removal")
								@Override
								public List<Component> getTooltip(ItemStack ingredient, TooltipFlag tooltipFlag) {
									return List.of();
								}
							});
				}
				itemMap.put(dropItem.itemStack(), dropItem);
			}
			case CompoundAction compoundAction -> {
				compoundAction.getChildActions().filter(it -> !it.hidden()).forEach(child -> buildActionSlot(
						builder,
						slotBuilder,
						child,
						itemMap));
			}
			default -> {
				slotBuilder.addIngredient(LycheeJEIPlugin.POST_ACTION, action);
				var outputItems = action.getOutputItems();
				if (!outputItems.isEmpty()) {
					builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).addItemStacks(outputItems);
				}
			}
		}
	}

	@Override
	public RecipeType<RecipeHolder<R>> getRecipeType() {
		return type;
	}

	@Override
	public Component getTitle() {
		return instance.title();
	}

	@Override
	public @Nullable IDrawable getIcon() {
		return icon;
	}

	@Override
	public int getWidth() {
		return instance.width();
	}

	@Override
	public int getHeight() {
		return instance.height();
	}

	private void actionGroup(IRecipeLayoutBuilder builder, R recipe, float x, float y) {
		slotGroup(builder, x, y, recipe.postActions().stream().filter(it -> !it.hidden()).toList(), this::actionSlot);
	}

	private void ingredientGroup(IRecipeLayoutBuilder builder, R recipe, float x, float y) {
		var ingredients = RVs.generateShapelessInputs(recipe);
		slotGroup(
				builder, x + 1, y + 1, ingredients, (layout0, ingredient, x0, y0) -> {
					var items = ingredient.ingredient.getItems();
					var slotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, (int) x0, (int) y0);
					slotBuilder.addItemStacks(Arrays.stream(items)
							.map(it -> ingredient.count == 1 ? it : it.copy())
							.peek(it -> it.setCount(ingredient.count))
							.toList());
					slotBuilder.setBackground(LycheeJEIPlugin.slot(ingredient.type), -1, -1);
					if (!ingredient.tooltips.isEmpty()) {
						slotBuilder.addRichTooltipCallback((stack, tooltip) -> tooltip.addAll(ingredient.tooltips));
					}
				});
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<R> recipeHolder, IFocusGroup focuses) {
		var layoutBuilder = new RvCategoryLayoutBuilder.Wrapped(instance) {
			@Override
			protected void _actionGroup(ILycheeRecipe<?> recipe, Vector2fc position) {
				RvCategoryAdapter.this.actionGroup(builder, recipeHolder.value(), position.x(), position.y());
			}

			@Override
			protected void _ingredientGroup(ILycheeRecipe<?> recipe, Vector2fc position) {
				RvCategoryAdapter.this.ingredientGroup(builder, recipeHolder.value(), position.x(), position.y());
			}
		};
		instance.type().configureLayout(layoutBuilder, recipeHolder);
		addBlockIngredients(builder, recipeHolder.value());
	}

	@Override
	public void createRecipeExtras(IRecipeExtrasBuilder builder, RecipeHolder<R> recipe, IFocusGroup focuses) {
		IRecipeCategory.super.createRecipeExtras(builder, recipe, focuses);
		var widgetBuilder = new RvCategoryWidgetBuilder(instance) {
			@Override
			public void addElement(RenderElement element) {
				var adapter = new RenderElementAdapter(element);
				builder.addWidget(adapter);
				if (element instanceof GuiEventListener) {
					builder.addGuiEventListener(adapter);
				}
			}
		};
		instance.configureDecorations(widgetBuilder, recipe);
	}

	@FunctionalInterface
	interface SlotLayoutFunction<T> {
		void apply(IRecipeLayoutBuilder builder, T item, float x, float y);
	}
}
