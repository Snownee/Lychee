package snownee.lychee.recipes.random_block_ticking;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.kiwi.loader.Platform;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.mixin.ChunkMapAccess;
import snownee.lychee.util.recipe.BlockKeyableRecipeType;

public class RandomBlockTickingRecipeType extends
		BlockKeyableRecipeType<LycheeRecipeContext, RandomBlockTickingRecipe> {

	public RandomBlockTickingRecipeType(
			String name,
			Class<RandomBlockTickingRecipe> clazz,
			@Nullable LootContextParamSet paramSet
	) {
		super(name, clazz, paramSet);
	}

	@Override
	public void refreshCache() {
		boolean prevEmpty = isEmpty();
		super.refreshCache();
		if (prevEmpty && isEmpty()) {
			return;
		}
		for (Block block : BuiltInRegistries.BLOCK) {
			((RandomlyTickable) block).lychee$setTickable(has(block));
		}
		MinecraftServer server = Platform.getServer();
		if (server == null) {
			return;
		}
		for (var level : server.getAllLevels()) {
			for (var chunkHolder : ((ChunkMapAccess) level.getChunkSource().chunkMap).callGetChunks()) {
				LevelChunk chunk = chunkHolder.getTickingChunk();
				if (chunk != null) {
					for (var section : chunk.getSections()) {
						section.recalcBlockCounts();
					}
				}
			}
		}
	}

}
