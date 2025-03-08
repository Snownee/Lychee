package snownee.lychee.compat.rei;

import java.util.List;

import dev.architectury.event.EventResult;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.DisplayRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Arrow;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
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
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.kiwi.util.KUtil;
import snownee.lychee.Lychee;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.rei.category.CategoryProviders;
import snownee.lychee.compat.rei.category.LycheeCategory;
import snownee.lychee.compat.rei.display.AnvilCraftingDisplay;
import snownee.lychee.compat.rei.display.DisplayRegisters;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.compat.rei.elements.LycheeEntryWidget;
import snownee.lychee.compat.rei.elements.ScreenElementWidget;
import snownee.lychee.compat.rei.ingredient.PostActionIngredientHelper;
import snownee.lychee.compat.rv.RvCategory;
import snownee.lychee.compat.rv.RvPlugin;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

@REIPluginClient
public class LycheeREIPlugin implements REIClientPlugin {
	public static final ResourceLocation ID = Lychee.id("main");
	public static final EntryType<PostAction> POST_ACTION = EntryType.deferred(Lychee.id("post_action"));

	private final RvPlugin rvPlugin = new RvPlugin();

	@Override
	public void registerCategories(CategoryRegistry registry) {
		rvPlugin.init();
		for (RvCategory<?> rvCategory : rvPlugin.categories().values()) {
			var categoryProvider = CategoryProviders.get(rvCategory.type.id);
			if (categoryProvider == null) {
				Lychee.LOGGER.error("Missing category provider for {}", rvCategory.type.id);
				continue;
			}

			CategoryIdentifier<LycheeDisplay<ILycheeRecipe<LycheeContext>>> id = CategoryIdentifier.of(rvCategory.id);
			//noinspection unchecked
			var category = categoryProvider.get(id, (RvCategory<ILycheeRecipe<LycheeContext>>) rvCategory);
			registry.add(category);
			for (List<ItemStack> workstation : rvCategory.workstations()) {
				registry.addWorkstations(id, EntryIngredients.ofItemStacks(workstation));
			}
		}

		CategoryExtensionProvider<Display> extensionProvider = (display, category, lastView) -> {
			if (display instanceof LycheeDisplay<?> lycheeDisplay) {
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
								rect = new Rect2i(
										arrow.getBounds().getCenterX() - bounds.getX() - 4,
										Math.max(arrow.getY() - bounds.getY() - 9, 4),
										8,
										8);
								break;
							}
						}
						if (rect != null) {
							LycheeCategory.createInfoBadgeIfNeeded(widgets, lycheeDisplay, bounds.getLocation(), rect);
						}
						return widgets;
					}
				};
			}
			return lastView;
		};
		registry.get(CategoryIdentifier.of("plugins/crafting")).registerExtension(extensionProvider);
		registry.get(CategoryIdentifier.of("plugins/anvil")).registerExtension(extensionProvider);
	}

	@Override
	public void registerDisplays(DisplayRegistry registry) {
		for (RvCategory<?> rvCategory : rvPlugin.categories().values()) {
			DisplayRegisters.DisplayRegister<ILycheeRecipe<LycheeContext>> displayRegister = DisplayRegisters.get(rvCategory.type.id);
			CategoryIdentifier<LycheeDisplay<ILycheeRecipe<LycheeContext>>> id = CategoryIdentifier.of(rvCategory.id);
			//noinspection unchecked
			displayRegister.consume(registry, id, (RvCategory<ILycheeRecipe<LycheeContext>>) rvCategory);
		}

		try {
			KUtil.getRecipes(RecipeTypes.ANVIL_CRAFTING).stream()
					.filter(it ->
							!it.value().output().isEmpty() &&
									!it.value().isSpecial() && !it.value().hideInRecipeViewer())
					.map(AnvilCraftingDisplay::new)
					.forEach(registry::add);
		} catch (Throwable e) {
			Lychee.LOGGER.error("", e);
		}

		registry.registerVisibilityPredicate((DisplayCategory<?> category, Display display) -> {
			if (display instanceof LycheeDisplay<?> lycheeDisplay && lycheeDisplay.recipe().hideInRecipeViewer()) {
				return EventResult.interruptFalse();
			}
			return EventResult.pass();
		});
	}

	public static Rectangle offsetRect(Point startPoint, Rect2i rect) {
		return new Rectangle(startPoint.x + rect.getX(), startPoint.y + rect.getY(), rect.getWidth(), rect.getHeight());
	}

	public static LycheeEntryWidget slot(Point startPoint, int x, int y, SlotType slotType) {
		LycheeEntryWidget widget = new LycheeEntryWidget(new Point(startPoint.x + x + 1, startPoint.y + y + 1));
		widget.background(slotType.element);
		return widget;
	}

	@Override
	public void registerEntryTypes(EntryTypeRegistry registration) {
		registration.register(POST_ACTION, new PostActionIngredientHelper());
	}

	public enum SlotType {
		NORMAL(AllGuiTextures.JEI_SLOT),
		CHANCE(AllGuiTextures.JEI_CHANCE_SLOT),
		CATALYST(AllGuiTextures.JEI_CATALYST_SLOT);

		final ScreenElement element;

		SlotType(AllGuiTextures element) {
			this.element = new ScreenElementWidget(element).element;
		}
	}
}
