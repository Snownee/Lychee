package snownee.lychee.compat.rei;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import dev.architectury.event.EventResult;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.DisplayRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Arrow;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.WidgetWithBounds;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.category.extension.CategoryExtensionProvider;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayCategoryView;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import org.jetbrains.annotations.NotNull;

import snownee.lychee.Lychee;
import snownee.lychee.LycheeTags;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.compat.JEIREI.CategoryCreationContext;
import snownee.lychee.compat.rei.category.BaseREICategory;
import snownee.lychee.compat.rei.category.BlockCrushingRecipeCategory;
import snownee.lychee.compat.rei.category.BlockExplodingRecipeCategory;
import snownee.lychee.compat.rei.category.BlockInteractionRecipeCategory;
import snownee.lychee.compat.rei.category.DripstoneRecipeCategory;
import snownee.lychee.compat.rei.category.ItemBurningRecipeCategory;
import snownee.lychee.compat.rei.category.ItemExplodingRecipeCategory;
import snownee.lychee.compat.rei.category.ItemInsideRecipeCategory;
import snownee.lychee.compat.rei.category.LightningChannelingRecipeCategory;
import snownee.lychee.compat.rei.display.AnvilCraftingDisplay;
import snownee.lychee.compat.rei.display.BaseREIDisplay;
import snownee.lychee.compat.rei.display.DisplayRecipeProvider;
import snownee.lychee.compat.rei.ingredient.PostActionIngredientHelper;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.BlockKeyRecipe;
import snownee.lychee.core.recipe.ILycheeRecipe;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.CommonProxy;

@REIPluginClient
public class REICompat implements REIClientPlugin {

	public static final ResourceLocation UID = new ResourceLocation(Lychee.ID, "main");
	public static final EntryType<PostAction> POST_ACTION = EntryType.deferred(new ResourceLocation(Lychee.ID, "post_action"));
	public static final Map<ResourceLocation, Map<ResourceLocation, BaseREICategory<?, ?, ?>>> CATEGORIES = Maps.newHashMap();
	public static final List<Consumer<Map<ResourceLocation, Function<CategoryCreationContext, BaseREICategory<?, ?, ?>>>>> CATEGORY_FACTORY_PROVIDERS = Lists.newArrayList();
	public static final List<Consumer<Map<ResourceLocation, BiFunction<LycheeRecipe<?>, CategoryIdentifier<?>, BaseREIDisplay<?>>>>> DISPLAY_FACTORY_PROVIDERS = Lists.newArrayList();

	public static void addCategoryFactoryProvider(Consumer<Map<ResourceLocation, Function<CategoryCreationContext, BaseREICategory<?, ?, ?>>>> provider) {
		CATEGORY_FACTORY_PROVIDERS.add(provider);
	}

	public static void addDisplayFactoryProvider(Consumer<Map<ResourceLocation, BiFunction<LycheeRecipe<?>, CategoryIdentifier<?>, BaseREIDisplay<?>>>> provider) {
		DISPLAY_FACTORY_PROVIDERS.add(provider);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void registerCategories(CategoryRegistry registration) {
		Map<ResourceLocation, Function<CategoryCreationContext, BaseREICategory<?, ?, ?>>> factories = Maps.newHashMap();
		factories.put(RecipeTypes.ITEM_BURNING.categoryId, $ -> new ItemBurningRecipeCategory(RecipeTypes.ITEM_BURNING));
		factories.put(RecipeTypes.ITEM_INSIDE.categoryId, $ -> new ItemInsideRecipeCategory(RecipeTypes.ITEM_INSIDE, AllGuiTextures.JEI_DOWN_ARROW));
		factories.put(RecipeTypes.BLOCK_INTERACTING.categoryId, $ -> {
			ScreenElement mainIcon = $.recipes().stream().map(LycheeRecipe::getType).anyMatch(Predicate.isEqual(RecipeTypes.BLOCK_INTERACTING)) ? AllGuiTextures.RIGHT_CLICK : AllGuiTextures.LEFT_CLICK;
			return new BlockInteractionRecipeCategory((List) List.of(RecipeTypes.BLOCK_INTERACTING, RecipeTypes.BLOCK_CLICKING), mainIcon);
		});
		factories.put(RecipeTypes.BLOCK_CRUSHING.categoryId, $ -> new BlockCrushingRecipeCategory(RecipeTypes.BLOCK_CRUSHING));
		factories.put(RecipeTypes.LIGHTNING_CHANNELING.categoryId, $ -> new LightningChannelingRecipeCategory(RecipeTypes.LIGHTNING_CHANNELING));
		factories.put(RecipeTypes.ITEM_EXPLODING.categoryId, $ -> new ItemExplodingRecipeCategory(RecipeTypes.ITEM_EXPLODING));
		factories.put(RecipeTypes.BLOCK_EXPLODING.categoryId, $ -> new BlockExplodingRecipeCategory(RecipeTypes.BLOCK_EXPLODING, GuiGameElement.of(Items.TNT)));
		factories.put(RecipeTypes.DRIPSTONE_DRIPPING.categoryId, $ -> new DripstoneRecipeCategory(RecipeTypes.DRIPSTONE_DRIPPING));
		CATEGORY_FACTORY_PROVIDERS.forEach($ -> $.accept(factories));

		JEIREI.registerCategories(factories::containsKey, (categoryId, context) -> {
			BaseREICategory<?, ?, ?> category = factories.get(categoryId).apply(context);
			category.categoryIdentifier = CategoryIdentifier.of(JEIREI.composeCategoryIdentifier(categoryId, context.group()));
			category.initialRecipes = (List) context.recipes();
			category.icon = category.createIcon((List) context.recipes());
			registration.add(category);
			CATEGORIES.computeIfAbsent(categoryId, $ -> Maps.newHashMap()).put(context.group(), category);
		});

		forEachCategories(RecipeTypes.BLOCK_CRUSHING, $ -> {
			/* off */
			$.initialRecipes.stream()
					.map(BlockKeyRecipe::getBlock)
					.distinct()
					.map(BlockPredicateHelper::getMatchedBlocks)
					.flatMap(Collection::stream)
					.distinct()
					.map(ItemLike::asItem)
					.filter(Predicate.not(Items.AIR::equals))
					.map(Item::getDefaultInstance)
					.forEach($$ -> registration.addWorkstations($.getCategoryIdentifier(), EntryStacks.of($$)));
			/* on */
		});
		forEachCategories(RecipeTypes.LIGHTNING_CHANNELING, $ -> {
			registration.addWorkstations($.getCategoryIdentifier(), EntryStacks.of(Items.LIGHTNING_ROD));
		});
		for (Item item : CommonProxy.tagElements(BuiltInRegistries.ITEM, LycheeTags.ITEM_EXPLODING_CATALYSTS)) {
			forEachCategories(RecipeTypes.ITEM_EXPLODING, $ -> {
				registration.addWorkstations($.getCategoryIdentifier(), EntryStacks.of(item.getDefaultInstance()));
			});
		}
		for (Item item : CommonProxy.tagElements(BuiltInRegistries.ITEM, LycheeTags.BLOCK_EXPLODING_CATALYSTS)) {
			forEachCategories(RecipeTypes.BLOCK_EXPLODING, $ -> {
				registration.addWorkstations($.getCategoryIdentifier(), EntryStacks.of(item.getDefaultInstance()));
			});
		}
		forEachCategories(RecipeTypes.DRIPSTONE_DRIPPING, $ -> {
			registration.addWorkstations($.getCategoryIdentifier(), EntryStacks.of(Items.POINTED_DRIPSTONE));
		});

		CategoryExtensionProvider<Display> extensionProvider = (display, category, lastView) -> {
			if (display instanceof DisplayRecipeProvider d) {
				ILycheeRecipe<?> recipe = d.recipe();
				return new DisplayCategoryView<>() {
					@Override
					public DisplayRenderer getDisplayRenderer(Display display) {
						return lastView.getDisplayRenderer(display);
					}

					@Override
					public List<Widget> setupDisplay(Display display, Rectangle bounds) {
						List<Widget> widgets = lastView.setupDisplay(display, bounds);
						Rect2i rect = null;
						for (Widget widget : widgets) {
							if (widget instanceof Arrow arrow) {
								rect = new Rect2i(arrow.getBounds().getCenterX() - bounds.getX() - 4, Math.max(arrow.getY() - bounds.getY() - 9, 4), 8, 8);
								break;
							}
						}
						if (rect != null) {
							BaseREICategory.drawInfoBadge(widgets, recipe, bounds.getLocation(), rect);
						}
						return widgets;
					}
				};
			}
			return lastView;
		};
		registration.get(CategoryIdentifier.of("minecraft", "plugins/crafting")).registerExtension(extensionProvider);
		registration.get(CategoryIdentifier.of("minecraft", "plugins/anvil")).registerExtension(extensionProvider);
	}

	private static <C extends LycheeContext, T extends LycheeRecipe<C>, D extends BaseREIDisplay<T>> void forEachCategories(LycheeRecipeType<C, T> recipeType, Consumer<BaseREICategory<C, T, D>> consumer) {
		CATEGORIES.getOrDefault(recipeType.categoryId, Map.of()).values().stream().map($ -> (BaseREICategory<C, T, D>) $).forEach(consumer);
	}

	@Override
	public void registerDisplays(DisplayRegistry registration) {
		Map<ResourceLocation, BiFunction<LycheeRecipe<?>, CategoryIdentifier<?>, BaseREIDisplay<?>>> factories = Maps.newHashMap();
		CATEGORIES.keySet().forEach($ -> registerDisplayFactory(factories, $, BaseREIDisplay::new));
		DISPLAY_FACTORY_PROVIDERS.forEach($ -> $.accept(factories));

		CATEGORIES.values().forEach($ -> {
			$.values().forEach($$ -> {
				var category = (BaseREICategory<LycheeContext, LycheeRecipe<LycheeContext>, BaseREIDisplay<LycheeRecipe<LycheeContext>>>) $$;
				category.initialRecipes.forEach($$$ -> {
					ResourceLocation categoryId = $$$.getType().categoryId;
					registration.add(factories.get(categoryId).apply($$$, $$.categoryIdentifier));
				});
			});
		});

		try {
			CommonProxy.recipes(RecipeTypes.ANVIL_CRAFTING).stream().filter($ -> {
				return !$.getResultItem().isEmpty() && !$.isSpecial() && $.showInRecipeViewer();
			}).map(AnvilCraftingDisplay::new).forEach(registration::add);
		} catch (Throwable e) {
			Lychee.LOGGER.error("", e);
		}

		registration.registerVisibilityPredicate((DisplayCategory<?> category, Display display) -> {
			if (display instanceof DisplayRecipeProvider d && !d.recipe().showInRecipeViewer()) {
				return EventResult.interruptFalse();
			}
			return EventResult.pass();
		});
	}

	@SuppressWarnings("rawtypes")
	public static <C extends LycheeContext, T extends LycheeRecipe<C>, D extends BaseREIDisplay<T>> void registerDisplayFactory(Map<ResourceLocation, BiFunction<LycheeRecipe<?>, CategoryIdentifier<?>, BaseREIDisplay<?>>> factories, ResourceLocation id, BiFunction<T, CategoryIdentifier<D>, ? extends D> factory) {
		factories.put(id, (BiFunction) factory);
	}

	@Override
	public void registerEntryTypes(EntryTypeRegistry registration) {
		registration.register(POST_ACTION, new PostActionIngredientHelper());
	}

	private static final Map<AllGuiTextures, ScreenElementWrapper> elMap = Maps.newIdentityHashMap();

	public static ScreenElementWrapper el(AllGuiTextures element) {
		return elMap.computeIfAbsent(element, ScreenElementWrapper::new);
	}

	public enum SlotType {
		NORMAL(AllGuiTextures.JEI_SLOT),
		CHANCE(AllGuiTextures.JEI_CHANCE_SLOT),
		CATALYST(AllGuiTextures.JEI_CATALYST_SLOT);

		final ScreenElement element;

		SlotType(AllGuiTextures element) {
			this.element = el(element).element;
		}
	}

	public static LEntryWidget slot(Point startPoint, int x, int y, SlotType slotType) {
		LEntryWidget widget = new LEntryWidget(new Point(startPoint.x + x + 1, startPoint.y + y + 1));
		widget.background(slotType.element);
		return widget;
	}

	public static class ScreenElementWrapper extends WidgetWithBounds {

		public final Rectangle bounds = new Rectangle(16, 16);
		private final ScreenElement element;

		private ScreenElementWrapper(AllGuiTextures element) {
			this.element = element;
			bounds.width = element.width;
			bounds.height = element.height;
		}

		public ScreenElementWrapper(RenderElement element) {
			this.element = element;
			bounds.width = element.getWidth();
			bounds.height = element.getHeight();
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
			element.render(graphics, bounds.x, bounds.y);
		}

		@Override
		public Rectangle getBounds() {
			return bounds;
		}

		@Override
		public @NotNull List<? extends GuiEventListener> children() {
			return Collections.emptyList();
		}

	}

	public static Rectangle offsetRect(Point startPoint, Rect2i rect) {
		return new Rectangle(startPoint.x + rect.getX(), startPoint.y + rect.getY(), rect.getWidth(), rect.getHeight());
	}

}
