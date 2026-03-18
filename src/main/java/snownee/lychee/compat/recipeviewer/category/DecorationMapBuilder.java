package snownee.lychee.compat.recipeviewer.category;

import java.util.function.Function;
import java.util.function.Predicate;

import org.joml.Vector2fc;

import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public abstract class DecorationMapBuilder<R extends ILycheeRecipe<LycheeContext>> {
	public abstract void put(String key, RvCategoryDecoration<R> decoration);

	public abstract void condition(String key, Predicate<R> predicate);

	public void info(Function<R, Vector2fc> positioner) {
		condition("info", RvCategory::needInfo);
		put(
				"info", (builder, recipeHolder) -> builder.addElement(RvCategory.infoIcon(recipeHolder)
						.at(positioner.apply(recipeHolder.value()))));
	}

	public void consumeBlockInput(Function<R, Vector2fc> positioner) {
		condition("consume_block_in", RvCategory::needConsumeBlockInput);
		put("consume_block_in",
				(builder, recipeHolder) -> builder.addElement(
						RvCategory.consumeBlockInputIcon().at(positioner.apply(recipeHolder.value()))));
	}
}
