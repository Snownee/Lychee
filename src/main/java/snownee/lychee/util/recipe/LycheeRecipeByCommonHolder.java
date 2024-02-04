package snownee.lychee.util.recipe;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.advancements.critereon.MinMaxBounds;
import snownee.lychee.util.action.PostAction;

public interface LycheeRecipeByCommonHolder<T extends LycheeRecipeByCommonHolder<T>> extends LycheeRecipe<T> {
	LycheeRecipeCommonHolder recipeCommonHolder();

	@Override
	default boolean hideInRecipeViewer() {
		return recipeCommonHolder().hideInRecipeViewer();
	}

	@Override
	default boolean ghost() {
		return recipeCommonHolder().ghost();
	}

	@Override
	default @Nullable String comment() {
		return recipeCommonHolder().comment();
	}

	@Override
	default String group() {
		return recipeCommonHolder().group();
	}

	@Override
	default List<PostAction<?>> postActions() {
		return recipeCommonHolder().postActions();
	}

	@Override
	default MinMaxBounds.Ints maxRepeats() {
		return recipeCommonHolder().maxRepeats();
	}
}
