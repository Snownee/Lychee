package snownee.lychee.recipes;

import java.util.Set;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.block.Block;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipeType;

public class DripstoneRecipeType extends BlockKeyableRecipeType<DripstoneRecipe> {

	private final Set<Block> allSources = Sets.newHashSet();

	public DripstoneRecipeType(String name, Class<DripstoneRecipe> clazz, @Nullable ContextKeySet paramSet) {
		super(name, clazz, paramSet);
	}

	@Override
	@MustBeInvokedByOverriders
	public void refreshCache(RecipeMap recipeMap) {
		super.refreshCache(recipeMap);
		allSources.clear();
		for (var recipe : recipes) {
			allSources.addAll(BlockPredicateExtensions.matchedBlocks(recipe.value().sourceBlock));
		}
	}

	public boolean hasSource(Block block) {
		return allSources.contains(block);
	}

}
