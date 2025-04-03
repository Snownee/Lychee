package snownee.lychee.compat.recipeviewer.rei;

import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector2fc;

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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.kiwi.util.KUtil;
import snownee.lychee.Lychee;
import snownee.lychee.RecipeTypes;
import snownee.lychee.compat.recipeviewer.RvPlugin;
import snownee.lychee.compat.recipeviewer.SlotType;
import snownee.lychee.compat.recipeviewer.category.AbstractRvCategory;
import snownee.lychee.compat.recipeviewer.category.RvCategory;
import snownee.lychee.compat.recipeviewer.rei.category.ReiRvHelper;
import snownee.lychee.compat.recipeviewer.rei.category.RvCategoryAdapter;
import snownee.lychee.compat.recipeviewer.rei.display.AnvilCraftingDisplay;
import snownee.lychee.compat.recipeviewer.rei.display.DisplayRegisters;
import snownee.lychee.compat.recipeviewer.rei.display.LycheeDisplay;
import snownee.lychee.compat.recipeviewer.rei.element.LycheeEntryWidget;
import snownee.lychee.compat.recipeviewer.rei.element.RenderElementAdapter;
import snownee.lychee.compat.recipeviewer.rei.ingredient.PostActionIngredientHelper;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class LycheeREIPlugin implements REIClientPlugin {
	public static final ResourceLocation ID = Lychee.id("main");
	public static final EntryType<PostAction> POST_ACTION = EntryType.deferred(Lychee.id("post_action"));

	private final RvPlugin<ReiRvHelper> rvPlugin = new RvPlugin<>(ReiRvHelper.INSTANCE);

	public static LycheeEntryWidget slot(Vector2fc startPoint, int x, int y, SlotType slotType) {
		LycheeEntryWidget widget = new LycheeEntryWidget(new Point(startPoint.x() + x + 1, startPoint.y() + y + 1));
		widget.background(slotType.sprite);
		return widget;
	}

	@Override
	public void registerDisplays(DisplayRegistry registry) {
		for (RvCategory<?> rvCategory : rvPlugin.categories().values()) {
			var displayRegister = DisplayRegisters.get(rvCategory.type().id);
			var id = CategoryIdentifier.<LycheeDisplay<ILycheeRecipe<LycheeContext>>>of(rvCategory.id());
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
			if (display instanceof LycheeDisplay<?> lycheeDisplay && lycheeDisplay.recipe().value().hideInRecipeViewer()) {
				return EventResult.interruptFalse();
			}
			return EventResult.pass();
		});
	}

	@Override
	public void registerCategories(CategoryRegistry registry) {
		rvPlugin.init();
		for (var rvCategory : rvPlugin.categories().values()) {
			var category = new RvCategoryAdapter<>(rvCategory);
			registry.add(category);
			for (List<ItemStack> workstation : rvCategory.workstations()) {
				registry.addWorkstations(category.getCategoryIdentifier(), EntryIngredients.ofItemStacks(workstation));
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
						Vector2fc infoPosition = null;
						for (Widget widget : widgets) {
							if (widget instanceof Arrow arrow) {
								infoPosition = new Vector2f(
										arrow.getBounds().getCenterX() - bounds.getX() - 4,
										Math.max(arrow.getY() - bounds.getY() - 9, 4));
								break;
							}
						}
						if (infoPosition != null && AbstractRvCategory.needInfoIcon(lycheeDisplay.recipe().value())) {
							widgets.add(new RenderElementAdapter(
									AbstractRvCategory.getRecipeInfoIcon(lycheeDisplay.recipe())
											.at(infoPosition)
											.offset(bounds.x, bounds.y)));
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
	public void registerEntryTypes(EntryTypeRegistry registration) {
		registration.register(POST_ACTION, new PostActionIngredientHelper());
	}
}