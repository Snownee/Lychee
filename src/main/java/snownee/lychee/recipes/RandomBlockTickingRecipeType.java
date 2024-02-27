package snownee.lychee.recipes;

import org.apache.commons.lang3.stream.Streams;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.kiwi.loader.Platform;
import snownee.lychee.mixin.ChunkMapAccess;
import snownee.lychee.util.RandomlyTickable;
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
	public void refreshCache() {
		var prevEmpty = isEmpty();
		super.refreshCache();
		if (prevEmpty && isEmpty()) {
			return;
		}
		for (var block : BuiltInRegistries.BLOCK) {
			((RandomlyTickable) block).lychee$setTickable(has(block));
		}
		var server = Platform.getServer();
		if (server == null) {
			return;
		}
		Streams.of(server.getAllLevels())
				.flatMap(it -> Streams.of(((ChunkMapAccess) it.getChunkSource().chunkMap).callGetChunks()))
				.filter(it -> it.getTickingChunk() != null)
				.flatMap(it -> Streams.of(it.getTickingChunk().getSections()))
				.forEach(LevelChunkSection::recalcBlockCounts);
	}
}
