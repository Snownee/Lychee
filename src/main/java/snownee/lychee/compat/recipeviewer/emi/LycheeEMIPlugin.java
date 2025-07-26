package snownee.lychee.compat.recipeviewer.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.compat.recipeviewer.RvPlugin;
import snownee.lychee.compat.recipeviewer.emi.category.EmiRvHelper;
import snownee.lychee.compat.recipeviewer.emi.category.RvCategoryAdapter;
import snownee.lychee.compat.recipeviewer.emi.recipe.EmiRecipeAdapter;

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
	}
}
