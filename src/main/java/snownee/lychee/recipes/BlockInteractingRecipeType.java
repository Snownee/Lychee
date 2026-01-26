package snownee.lychee.recipes;

import java.util.Comparator;

import org.jspecify.annotations.Nullable;

import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipeType;

public class BlockInteractingRecipeType<T extends BlockInteractingRecipe> extends BlockKeyableRecipeType<T> {
	public BlockInteractingRecipeType(
			String name,
			Class<T> clazz,
			@Nullable ContextKeySet paramSet) {
		super(name, clazz, paramSet);
	}

	@Override
	public Comparator<RecipeHolder<T>> comparator() {
		return Comparator.comparing(
				RecipeHolder::value,
				Comparator.comparing((BlockInteractingRecipe $) -> !BlockPredicateExtensions.isAny($.blockPredicate()))
						.thenComparingInt($ -> $.inputs().size())
						.thenComparing($ -> !$.maxRepeats().isAny())
						.thenComparing(Recipe::isSpecial)
						.thenComparing($ -> !$.inputs()
								.getFirst()
								.map(SizedIngredient::ingredient)
								.map(CommonProxy::isSimpleIngredient)
								.orElse(false))
						.thenComparing($ -> !$.inputs()
								.getLast()
								.map(SizedIngredient::ingredient)
								.map(CommonProxy::isSimpleIngredient)
								.orElse(false))
						.reversed());
	}
}