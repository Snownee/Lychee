package snownee.lychee.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(ChunkMap.class)
public interface ChunkMapAccess {
	@Invoker
	void callForEachBlockTickingChunk(Consumer<LevelChunk> tickingChunkConsumer);
}
