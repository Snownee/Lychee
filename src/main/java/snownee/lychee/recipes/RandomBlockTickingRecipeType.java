package snownee.lychee.recipes;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.lychee.util.RandomlyTickable;
import snownee.lychee.util.predicates.BlockStateSet;
import snownee.lychee.util.recipe.BlockKeyableRecipeType;

public class RandomBlockTickingRecipeType extends BlockKeyableRecipeType<RandomBlockTickingRecipe> {
	public RandomBlockTickingRecipeType(
			String name,
			Class<RandomBlockTickingRecipe> clazz,
			@Nullable LootContextParamSet paramSet
	) {
		super(name, clazz, paramSet);
	}

	@Override
	@MustBeInvokedByOverriders
	public void refreshCache(RecipeManager recipeManager) {
		var prevEmpty = isEmpty();
		super.refreshCache(recipeManager);
		if (prevEmpty && recipes.isEmpty()) { // do not use isEmpty() directly because empty state is not updated yet
			return;
		}

		Predicate<BlockState> predicate = anyBlockRecipes.isEmpty() ? BlockStateSet.NONE : BlockStateSet.ANY;
		for (var block : BuiltInRegistries.BLOCK) {
			((RandomlyTickable) block).lychee$setTickable(predicate);
		}
		if (anyBlockRecipes.isEmpty()) {
			for (var entry : recipesByBlock.entrySet()) {
				Block block = entry.getKey();
				Stream<BlockPredicate> stream = entry.getValue()
						.stream()
						.map(RecipeHolder::value)
						.map(RandomBlockTickingRecipe::blockPredicate);
				((RandomlyTickable) block).lychee$setTickable(BlockStateSet.of(block, stream));
			}
		}
	}
}
