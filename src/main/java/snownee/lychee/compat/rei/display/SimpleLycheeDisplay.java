package snownee.lychee.compat.rei.display;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record SimpleLycheeDisplay<T extends ILycheeRecipe<?>>(
		RecipeHolder<T> recipe,
		CategoryIdentifier<? extends LycheeDisplay<T>> id) implements LycheeDisplay<T> {
	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return id;
	}
}
