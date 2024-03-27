package snownee.lychee.compat.rei.display;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record SimpleLycheeDisplay<T extends ILycheeRecipe<?>>(
		T recipe,
		CategoryIdentifier<? extends LycheeDisplay<T>> categoryIdentifier) implements LycheeDisplay<T> {
	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return categoryIdentifier;
	}
}
