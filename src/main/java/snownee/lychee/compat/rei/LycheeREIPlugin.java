package snownee.lychee.compat.rei;

import java.util.Collection;
import java.util.Objects;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.Lychee;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.rei.category.CategoryProviders;
import snownee.lychee.compat.rei.category.DisplayRegisters;
import snownee.lychee.compat.rei.category.IconProviders;
import snownee.lychee.compat.rei.category.LycheeDisplayCategory;
import snownee.lychee.compat.rei.category.WorkstationRegisters;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.compat.rei.elements.LEntryWidget;
import snownee.lychee.compat.rei.elements.ScreenElementWidget;
import snownee.lychee.compat.rei.ingredient.PostActionIngredientHelper;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class LycheeREIPlugin implements REIClientPlugin {
	public static final ResourceLocation ID = Lychee.id("main");
	public static final EntryType<PostAction> POST_ACTION = EntryType.deferred(Lychee.id("post_action"));

	private static ResourceLocation composeCategoryIdentifier(ResourceLocation categoryId, ResourceLocation group) {
		return new ResourceLocation(
				categoryId.getNamespace(),
				"%s/%s/%s".formatted(categoryId.getPath(), group.getNamespace(), group.getPath()));
	}

	private static ImmutableMultimap<CategoryIdentifier<? extends LycheeDisplay<?>>, RecipeHolder<? extends ILycheeRecipe<LycheeContext>>> generateCategories(
			LycheeRecipeType<LycheeContext, ? extends ILycheeRecipe<LycheeContext>> recipeType) {
		return recipeType
				.inViewerRecipes()
				.stream()
				.reduce(
						ImmutableMultimap.<CategoryIdentifier<? extends LycheeDisplay<?>>, RecipeHolder<? extends ILycheeRecipe<LycheeContext>>>builder(),
						(map, recipeHolder) -> {
							map.put(
									CategoryIdentifier.of(composeCategoryIdentifier(
											recipeType.categoryId,
											new ResourceLocation(recipeHolder.value().group()))),
									recipeHolder
							);
							return map;
						},
						(map, ignored) -> map)
				.build();
	}

	private final Multimap<ResourceLocation, CategoryHolder> categories = LinkedHashMultimap.create();

	@Override
	public void registerCategories(CategoryRegistry registry) {
		categories.clear();
		for (var recipeType : RecipeTypes.ALL) {
			if (!recipeType.hasStandaloneCategory) {
				continue;
			}

			var generatedCategories = generateCategories(recipeType);

			var categoryProvider = CategoryProviders.get(recipeType);

			if (categoryProvider == null) {
				Lychee.LOGGER.error("Missing category provider for {}", recipeType);
				continue;
			}

			generatedCategories.asMap().forEach((id, recipes) -> {
				var category = categoryProvider.get(
						(CategoryIdentifier) id,
						Objects.requireNonNull(IconProviders.get(recipeType), recipeType::toString).get(recipes),
						(Collection) recipes);
				categories.put(recipeType.categoryId, new CategoryHolder(category, (Collection) recipes));
				registry.add(category);
				var workstationRegister = WorkstationRegisters.get(recipeType);
				if (workstationRegister != null) {
					workstationRegister.consume(registry, category, (Collection) recipes);
				}
			});
		}
	}

	@Override
	public void registerDisplays(DisplayRegistry registry) {
		categories.asMap().forEach((id, categories) -> {
			var displayRegister = DisplayRegisters.get(id);
			for (var category : categories) {
				displayRegister.consume(registry, (LycheeDisplayCategory) category.category, category.recipes);
			}
		});
	}

	public static Rectangle offsetRect(Point startPoint, Rect2i rect) {
		return new Rectangle(startPoint.x + rect.getX(), startPoint.y + rect.getY(), rect.getWidth(), rect.getHeight());
	}

	public record CategoryHolder(
			LycheeDisplayCategory<?> category,
			Collection<RecipeHolder<ILycheeRecipe<LycheeContext>>> recipes) {}

	public static LEntryWidget slot(Point startPoint, int x, int y, SlotType slotType) {
		LEntryWidget widget = new LEntryWidget(new Point(startPoint.x + x + 1, startPoint.y + y + 1));
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
