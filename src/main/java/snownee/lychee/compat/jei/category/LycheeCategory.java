package snownee.lychee.compat.jei.category;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.InputConstants;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.gui.builder.IIngredientAcceptor;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import snownee.lychee.Lychee;
import snownee.lychee.action.DropItem;
import snownee.lychee.action.PlaceBlock;
import snownee.lychee.action.RandomSelect;
import snownee.lychee.category.SpriteElement;
import snownee.lychee.category.SpriteElementRenderer;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.compat.jei.LycheeJEIPlugin;
import snownee.lychee.compat.jei.elements.InteractiveWidget;
import snownee.lychee.compat.rv.RVs;
import snownee.lychee.compat.rv.RvCategoryProvider;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.action.CompoundAction;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionRenderer;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.ILycheeRecipe;

public interface LycheeCategory<R extends ILycheeRecipe<LycheeContext>> extends RvCategoryProvider<R> {
	static void addBlockIngredients(IRecipeLayoutBuilder builder, ILycheeRecipe<LycheeContext> recipe) {
		addBlockIngredients(builder, recipe.getBlockInputs(), RecipeIngredientRole.INPUT);
		addBlockIngredients(builder, recipe.getBlockOutputs(), RecipeIngredientRole.OUTPUT);
	}

	static void addBlockIngredients(IRecipeLayoutBuilder builder, Iterable<BlockPredicate> blocks, RecipeIngredientRole role) {
		for (BlockPredicate block : blocks) {
			List<ItemStack> items = BlockPredicateExtensions.matchedItemStacks(block);
			Set<Fluid> fluids = BlockPredicateExtensions.matchedFluids(block);
			if (!items.isEmpty() || !fluids.isEmpty()) {
				IIngredientAcceptor<?> acceptor = builder.addInvisibleIngredients(role);
				acceptor.addItemStacks(items);
				fluids.forEach(fluid -> acceptor.addFluidStack(
						fluid,
						LycheeJEIPlugin.helpers.getPlatformFluidHelper().bucketVolume()));
			}
		}
	}


	static <T> void slotGroup(
			IRecipeLayoutBuilder builder,
			int x,
			int y,
			int offset,
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
				layoutFunction.apply(builder, items.get(index), offset + index, x + j * 19, y + i * 19);
				++index;
			}
		}
	}

	static void actionSlot(IRecipeLayoutBuilder builder, PostAction action, int index, int x, int y) {
		var slotBuilder = builder.addSlot(RecipeIngredientRole.OUTPUT, x + 1, y + 1);
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
				list = PostActionRenderer.getTooltipsFromRandom(randomSelect, (PostAction) raw, player);
			} else {
				list = PostActionRenderer.of(action).getTooltips(action, player);
			}
			tooltip.addAll(list);
		});
		slotBuilder.setBackground(
				LycheeJEIPlugin.slot(action.conditions().conditions().isEmpty() ?
						LycheeJEIPlugin.SlotType.NORMAL :
						LycheeJEIPlugin.SlotType.CHANCE), -1, -1);
	}

	static void buildActionSlot(
			IRecipeLayoutBuilder builder,
			IRecipeSlotBuilder slotBuilder,
			PostAction action,
			Map<ItemStack, PostAction> itemMap
	) {
		switch (action) {
			case DropItem dropItem -> {
				slotBuilder.addItemStack(dropItem.stack());
				itemMap.put(dropItem.stack(), dropItem);
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

	static void createInfoBadgeIfNeeded(IRecipeExtrasBuilder builder, RecipeHolder<? extends ILycheeRecipe<?>> recipeHolder, Rect2i rect) {
		ILycheeRecipe<?> recipe = recipeHolder.value();
		if (recipe.conditions().conditions().isEmpty() && !recipe.comment().map(it -> !Strings.isNullOrEmpty(it)).orElse(false)) {
			return;
		}
		ScreenRectangle bounds = new ScreenRectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
		InteractiveWidget widget = new InteractiveWidget(bounds);
		widget.setOnClick((w, button) -> ClientProxy.postInfoBadgeClickEvent(recipeHolder.value(), recipeHolder.id(), button));
		widget.setTooltipFunction($ -> RVs.getRecipeTooltip(recipeHolder.value()));
		widget.setRenderable((graphics, mouseX, mouseY, delta) -> {
			var matrixStack = graphics.pose();
			matrixStack.pushPose();
			matrixStack.translate(bounds.left(), bounds.top(), 0);
			matrixStack.scale(.5F, .5F, .5F);
			AllGuiTextures.INFO.render(graphics, 0, 0);
			matrixStack.popPose();
		});
		builder.addWidget(widget);
		builder.addGuiEventListener(widget);
	}

	static <T extends ILycheeRecipe<LycheeContext>> void addRemoveInputBlock(
			int x,
			int y,
			IRecipeExtrasBuilder builder,
			T recipe) {
		if (recipe.postActions().stream().noneMatch(it -> it instanceof PlaceBlock placeBlock && placeBlock.hidden())) {
			return;
		}
		var widget = new InteractiveWidget(new ScreenRectangle(x, y, 8, 8), true);
		builder.addWidget(widget);
		builder.addGuiEventListener(widget);
		widget.setRenderable(new SpriteElementRenderer(
				new SpriteElement(Lychee.id("exclamation_mark")),
				x,
				y,
				100,
				widget.getWidth(),
				widget.getHeight()));
		widget.setTooltipFunction(it -> List.of(Component.translatable("postAction.lychee.place.consume")));
	}

	Rect2i infoRect();

	default int contentWidth() {
		return 120;
	}

	default void createInfoBadgeIfNeeded(IRecipeExtrasBuilder builder, RecipeHolder<? extends ILycheeRecipe<?>> recipeHolder) {
		createInfoBadgeIfNeeded(builder, recipeHolder, infoRect());
	}

	default void actionGroup(IRecipeLayoutBuilder builder, R recipe, int x, int y) {
		slotGroup(
				builder,
				x,
				y,
				10000,
				recipe.postActions().stream().filter(it -> !it.hidden()).toList(),
				LycheeCategory::actionSlot);
	}

	default void ingredientGroup(IRecipeLayoutBuilder builder, R recipe, int x, int y) {
		var ingredients = RVs.generateShapelessInputs(recipe);
		slotGroup(
				builder, x + 1, y + 1, 0, ingredients, (layout0, ingredient, i, x0, y0) -> {
					var items = ingredient.ingredient.getItems();
					var slotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, x0, y0);
					slotBuilder.addItemStacks(Arrays.stream(items)
							.map(it -> ingredient.count == 1 ? it : it.copy())
							.peek(it -> it.setCount(ingredient.count))
							.toList());
					slotBuilder.setBackground(
							LycheeJEIPlugin.slot(ingredient.isCatalyst ?
									LycheeJEIPlugin.SlotType.CATALYST :
									LycheeJEIPlugin.SlotType.NORMAL), -1, -1);
					if (!ingredient.tooltips.isEmpty()) {
						slotBuilder.addRichTooltipCallback((stack, tooltip) -> tooltip.addAll(ingredient.tooltips));
					}
				});
	}


	static boolean clickBlock(BlockState state, InputConstants.Key input) {
		if (input.getType() != InputConstants.Type.MOUSE) {
			return false;
		}
		if (state.is(Blocks.CHIPPED_ANVIL) || state.is(Blocks.DAMAGED_ANVIL)) {
			state = Blocks.ANVIL.defaultBlockState();
		}
		var recipesGui = LycheeJEIPlugin.runtime.getRecipesGui();
		var focusFactory = LycheeJEIPlugin.helpers.getFocusFactory();
		var role = input.getValue() == 1 ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
		var stack = state.getBlock().asItem().getDefaultInstance();
		if (!stack.isEmpty()) {
			recipesGui.show(focusFactory.createFocus(role, VanillaTypes.ITEM_STACK, stack));
			return true;
		} else if (state.getBlock() instanceof LiquidBlock) {
			var fluidHelper = (IPlatformFluidHelper<IJeiFluidIngredient>) LycheeJEIPlugin.helpers.getPlatformFluidHelper();
			recipesGui.show(focusFactory.createFocus(
					role,
					fluidHelper.getFluidIngredientType(),
					fluidHelper.create(state.getFluidState().holder(), fluidHelper.bucketVolume())));
		}
		return false;
	}

	@FunctionalInterface
	interface SlotLayoutFunction<T> {
		void apply(IRecipeLayoutBuilder builder, T item, int index, int x, int y);
	}
}
