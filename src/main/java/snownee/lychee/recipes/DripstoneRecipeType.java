package snownee.lychee.recipes;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.lychee.util.predicates.BlockPredicateExtensions;
import snownee.lychee.util.recipe.BlockKeyableRecipeType;

public class DripstoneRecipeType extends BlockKeyableRecipeType<DripstoneRecipe> {

	private final Set<Block> allSources = Sets.newHashSet();

	public DripstoneRecipeType(String name, Class<DripstoneRecipe> clazz, @Nullable LootContextParamSet paramSet) {
		super(name, clazz, paramSet);
	}

	@Override
	public void refreshCache() {
		super.refreshCache();
		allSources.clear();
		for (var recipe : recipes) {
			allSources.addAll(BlockPredicateExtensions.matchedBlocks(recipe.value().sourceBlock));
		}
	}

	public boolean hasSource(Block block) {
		return allSources.contains(block);
	}

}
