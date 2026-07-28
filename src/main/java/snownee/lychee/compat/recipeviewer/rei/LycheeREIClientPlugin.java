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
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import snownee.lychee.Lychee;
import snownee.lychee.RecipeTypes;
import snownee.lychee.compat.recipeviewer.RvPlugin;
import snownee.lychee.compat.recipeviewer.SlotType;
import snownee.lychee.compat.recipeviewer.category.RvCategory;
import snownee.lychee.compat.recipeviewer.category.RvCategoryInstance;
import snownee.lychee.compat.recipeviewer.rei.category.ReiRvHelper;
import snownee.lychee.compat.recipeviewer.rei.category.RvCategoryAdapter;
import snownee.lychee.compat.recipeviewer.rei.display.AnvilCraftingDisplay;
import snownee.lychee.compat.recipeviewer.rei.display.LycheeDisplay;
import snownee.lychee.compat.recipeviewer.rei.display.SimpleLycheeDisplay;
import snownee.lychee.compat.recipeviewer.rei.element.LycheeEntryWidget;
import snownee.lychee.compat.recipeviewer.rei.element.RenderElementAdapter;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.action.PostAction;

public class LycheeREIClientPlugin implements REIClientPlugin {
	public static final Identifier ID = Lychee.id("main");
	public static final EntryType<PostAction> POST_ACTION = EntryType.deferred(Lychee.id("post_action"));
	private final RvPlugin<ReiRvHelper> rvPlugin = new RvPlugin<>(ReiRvHelper.INSTANCE);

	public static LycheeEntryWidget slot(Vector2fc startPoint, int x, int y, SlotType slotType) {
		LycheeEntryWidget widget = new LycheeEntryWidget(new Point(startPoint.x() + x + 1, startPoint.y() + y + 1));
		widget.background(slotType.sprite);
		return widget;
	}

	private static RecipeMap clientRecipes() {
		var connection = Minecraft.getInstance().getConnection();
		return connection == null ? RecipeMap.EMPTY : ClientProxy.recipes(connection.recipes());
	}

	@Override
	public void registerCategories(CategoryRegistry registry) {
		rvPlugin.init(clientRecipes());
		for (RvCategoryInstance<?> rvCategory : rvPlugin.categories().values()) {
			var category = new RvCategoryAdapter<>(rvCategory);
			registry.add(category);
			for (SlotDisplay display : rvCategory.workstations()) {
				registry.addWorkstations(category.getCategoryIdentifier(), EntryIngredients.ofSlotDisplay(display));
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
						if (infoPosition != null && RvCategory.needInfo(lycheeDisplay.recipeValue())) {
							widgets.add(new RenderElementAdapter(
									RvCategory.infoIcon((net.minecraft.world.item.crafting.RecipeHolder) lycheeDisplay.recipeHolder()).at(infoPosition),
									bounds.getLocation()));
						}
						return widgets;
					}
				};
			}
			return lastView;
		};
		registry.get((CategoryIdentifier) BuiltinPlugin.CRAFTING).registerExtension((CategoryExtensionProvider) extensionProvider);
		registry.get((CategoryIdentifier) BuiltinPlugin.ANVIL).registerExtension((CategoryExtensionProvider) extensionProvider);
	}

	@Override
	public void registerDisplays(DisplayRegistry registry) {
		for (RvCategoryInstance<?> instance : rvPlugin.categories().values()) {
			var id = CategoryIdentifier.of(instance.id());
			for (var recipe : instance.recipes()) {
				registry.add(new SimpleLycheeDisplay<>(recipe, id));
			}
		}

		RecipeMap recipeMap = clientRecipes();
		try {
			recipeMap.byType(RecipeTypes.ANVIL_CRAFTING).stream()
					.filter(it -> !it.value().isSpecial() && !it.value().hideInRecipeViewer())
					.map(AnvilCraftingDisplay::new)
					.forEach(registry::add);
		} catch (Throwable e) {
			Lychee.LOGGER.error("Error when registering REI anvil crafting recipes", e);
		}

		registry.registerVisibilityPredicate((DisplayCategory<?> category, Display display) -> {
			if (display instanceof LycheeDisplay<?> lycheeDisplay && lycheeDisplay.recipeValue().hideInRecipeViewer()) {
				return EventResult.interruptFalse();
			}
			return EventResult.pass();
		});
	}
}
