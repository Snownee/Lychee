package snownee.lychee.compat.recipeviewer.rei.category;

import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import snownee.lychee.compat.recipeviewer.RvHelper;

public class ReiRvHelper extends RvHelper {
	public static final ReiRvHelper INSTANCE = new ReiRvHelper();

	@Override
	public boolean openPage(ItemStack stack, boolean usageOrRecipe) {
		if (stack.isEmpty()) {
			return false;
		}
		var entry = EntryStacks.of(stack);
		return openPage(entry, usageOrRecipe);
	}

	@Override
	public boolean openPage(Fluid fluid, boolean usageOrRecipe) {
		var entry = EntryStacks.of(fluid);
		return openPage(entry, usageOrRecipe);
	}

	public boolean openPage(EntryStack<?> entry, boolean usageOrRecipe) {
		var searchBuilder = ViewSearchBuilder.builder();
		if (!usageOrRecipe) {
			searchBuilder.addRecipesFor(entry);
		} else {
			searchBuilder.addUsagesFor(entry);
		}
		searchBuilder.open();
		return true;
	}
}
