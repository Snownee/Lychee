package snownee.lychee.recipes;

import java.util.Comparator;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipeType;

public class BlockInteractingRecipeType<T extends BlockInteractingRecipe> extends BlockKeyableRecipeType<T> {
	public BlockInteractingRecipeType(
			String name,
			Class<T> clazz,
			@Nullable LootContextParamSet contextParamSet) {
		super(name, clazz, contextParamSet);
	}

	@Override
	public Comparator<RecipeHolder<T>> comparator() {
		return Comparator.comparing(
				RecipeHolder::value,
				Comparator.comparing((BlockInteractingRecipe $) -> !BlockPredicateExtensions.isAny($.blockPredicate()))
						.thenComparingInt($ -> $.getIngredients().size())
						.thenComparing($ -> !$.maxRepeats().isAny())
						.thenComparing(Recipe::isSpecial)
						.thenComparing($ -> !CommonProxy.isSimpleIngredient($.sizedIngredients().getFirst().ingredient()))
						.thenComparing($ -> !CommonProxy.isSimpleIngredient($.sizedIngredients().getLast().ingredient()))
						.reversed());
	}
}