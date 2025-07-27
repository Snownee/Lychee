package snownee.lychee.compat.recipeviewer.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.RecipeTypes;
import snownee.lychee.compat.recipeviewer.RvPlugin;
import snownee.lychee.compat.recipeviewer.category.RvCategory;
import snownee.lychee.compat.recipeviewer.emi.category.EmiRvHelper;
import snownee.lychee.compat.recipeviewer.emi.category.RvCategoryAdapter;
import snownee.lychee.compat.recipeviewer.emi.element.EmiWidgetAdapter;
import snownee.lychee.compat.recipeviewer.emi.recipe.EmiRecipeAdapter;
import snownee.lychee.compat.recipeviewer.emi.recipe.LycheeEmiAnvilRecipe;
import snownee.lychee.recipes.AnvilCraftingRecipe;
import snownee.lychee.recipes.ShapedCraftingRecipe;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class LycheeEMIPlugin implements EmiPlugin {
	private final RvPlugin<EmiRvHelper> rvPlugin = new RvPlugin<>(EmiRvHelper.INSTANCE);

	@Override
	public void register(EmiRegistry registry) {
		rvPlugin.init();
		for (var instance : rvPlugin.categories().values()) {
			RvCategoryAdapter<?> category = new RvCategoryAdapter<>(instance);
			registry.addCategory(category);
			for (var workstation : instance.workstations()) {
				registry.addWorkstation(category, EmiIngredient.of(workstation));
			}
			for (var recipe : instance.recipes()) {
				//noinspection all
				registry.addRecipe(new EmiRecipeAdapter<>(category, (RecipeHolder) recipe));
			}
		}

		for (RecipeHolder<AnvilCraftingRecipe> recipe : RecipeTypes.ANVIL_CRAFTING.inViewerRecipes()) {
			registry.addRecipe(new LycheeEmiAnvilRecipe(recipe));
		}
		registry.addRecipeDecorator(
				VanillaEmiRecipeCategories.CRAFTING, (emiRecipe, widgets) -> {
					RecipeHolder<?> recipeHolder = emiRecipe.getBackingRecipe();
					if (recipeHolder == null || !(recipeHolder.value() instanceof ShapedCraftingRecipe)) {
						return;
					}
					//noinspection unchecked
					addInfoIcon(widgets, (RecipeHolder<? extends ILycheeRecipe<?>>) recipeHolder, 60, 18);
				});
	}

	public static void addInfoIcon(WidgetHolder widgets, RecipeHolder<? extends ILycheeRecipe<?>> recipe, int x, int y) {
		if (!RvCategory.needInfo(recipe.value())) {
			return;
		}
		x += EmiTexture.EMPTY_ARROW.regionWidth / 2 - 4;
		y = Math.max(y - 9, 0);
		widgets.add(new EmiWidgetAdapter(RvCategory.infoIcon(recipe).at(x, y)));
	}
}
