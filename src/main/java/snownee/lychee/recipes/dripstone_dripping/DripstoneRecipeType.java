package snownee.lychee.recipes.dripstone_dripping;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.util.recipe.BlockInputLycheeRecipeType;

public class DripstoneRecipeType extends BlockInputLycheeRecipeType<DripstoneContext, DripstoneRecipe> {

	private final Set<Block> allSources = Sets.newHashSet();

	public DripstoneRecipeType(String name, Class<DripstoneRecipe> clazz, @Nullable LootContextParamSet paramSet) {
		super(name, clazz, paramSet);
	}

	@Override
	public void refreshCache() {
		super.refreshCache();
		allSources.clear();
		for (DripstoneRecipe recipe : recipes) {
			for (Block block : BlockPredicateHelper.getMatchedBlocks(recipe.getSourceBlock())) {
				allSources.add(block);
			}
		}
	}

	public boolean hasSource(Block block) {
		return allSources.contains(block);
	}

}
