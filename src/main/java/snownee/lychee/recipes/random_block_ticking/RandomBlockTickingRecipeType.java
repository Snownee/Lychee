package snownee.lychee.recipes.random_block_ticking;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.kiwi.loader.Platform;
import snownee.lychee.Lychee;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.mixin.ChunkMapAccess;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.recipe.BlockInputLycheeRecipeType;

public class RandomBlockTickingRecipeType extends
										  BlockInputLycheeRecipeType<LycheeRecipeContext, RandomBlockTickingRecipe> {

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
		if (CommonProxy.hasKiwi) {
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
		} else {
			String s = "Random block ticking recipes require Kiwi to be installed!";
			Lychee.LOGGER.warn(s);
			if (CommonProxy.isPhysicalClient()) {
				Minecraft client = Minecraft.getInstance();
				if (client.player != null) {
					client.player.sendSystemMessage(Component.literal(s));
				}
			}
		}
	}

}
