package snownee.lychee.compat.recipeviewer.rei.display;

import java.util.Optional;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record SimpleLycheeDisplay<T extends ILycheeRecipe<?>>(RecipeHolder<T> recipeHolder, CategoryIdentifier<?> id) implements LycheeDisplay<T> {
	@Override
	public CategoryIdentifier<?> getCategoryIdentifier() {
		return id;
	}

	@Override
	public T recipeValue() {
		return recipeHolder.value();
	}

	@Override
	public Optional<Identifier> getDisplayLocation() {
		return Optional.of(recipeHolder.id().identifier());
	}
}
