package snownee.lychee.recipes;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jspecify.annotations.Nullable;

import com.google.common.collect.Streams;

import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import snownee.kiwi.loader.Platform;
import snownee.lychee.mixin.ChunkMapAccess;
import snownee.lychee.util.RandomlyTickable;
import snownee.lychee.util.predicates.BlockStateSet;
import snownee.lychee.util.recipe.BlockKeyableRecipeType;

public class RandomBlockTickingRecipeType extends BlockKeyableRecipeType<RandomBlockTickingRecipe> {
	public RandomBlockTickingRecipeType(
			String name,
			Class<RandomBlockTickingRecipe> clazz,
			@Nullable ContextKeySet paramSet
	) {
		super(name, clazz, paramSet);
	}

	@Override
	@MustBeInvokedByOverriders
	public void refreshCache(RecipeMap recipeMap) {
		var prevEmpty = isEmpty();
		super.refreshCache(recipeMap);
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

		MinecraftServer server = Platform.getServer();
		//noinspection ConstantValue
		if (server == null) {
			return;
		}
		Streams.stream(server.getAllLevels())
				.flatMap(it -> {
					Stream.Builder<LevelChunk> builder = Stream.builder();
					((ChunkMapAccess) it.getChunkSource().chunkMap).callForEachBlockTickingChunk(builder::add);
					return builder.build();
				})
				.flatMap(it -> Stream.of(it.getSections()))
				.forEach(LevelChunkSection::recalcBlockCounts);
	}
}
