package snownee.lychee.compat.rei;

import java.util.Collection;

import com.google.common.collect.ImmutableMultimap;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.type.EntryType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.Lychee;
import snownee.lychee.RecipeTypes;
import snownee.lychee.compat.rei.category.CategoryProviders;
import snownee.lychee.compat.rei.category.IconProviders;
import snownee.lychee.compat.rei.category.WorkstationRegisters;
import snownee.lychee.compat.rei.display.LycheeDisplay;
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
											Lychee.id(recipeHolder.value().group()))),
									recipeHolder
							);
							return map;
						},
						(map, ignored) -> map)
				.build();
	}

	@Override
	public void registerCategories(CategoryRegistry registry) {
		for (var recipeType : RecipeTypes.ALL) {
			if (!recipeType.hasStandaloneCategory) {
				continue;
			}

			var generatedCategories = generateCategories(recipeType);

			// TODO For developing
			if (CategoryProviders.get(recipeType) == null) {
				continue;
			}

			generatedCategories.asMap().forEach((id, recipes) -> {
				var category = CategoryProviders.get(recipeType).get(
						(CategoryIdentifier) id,
						IconProviders.get(recipeType).get(recipes),
						(Collection) recipes);
				registry.add(category);
				var workstationRegister = WorkstationRegisters.get(recipeType);
				if (workstationRegister != null) {
					workstationRegister.consume(registry, category, (Collection) recipes);
				}
			});
		}
	}
}
