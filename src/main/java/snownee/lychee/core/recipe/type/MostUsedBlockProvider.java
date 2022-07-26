package snownee.lychee.core.recipe.type;

import net.minecraft.world.level.block.state.BlockState;
import snownee.lychee.util.Pair;

public interface MostUsedBlockProvider {

	Pair<BlockState, Integer> getMostUsedBlock();

}
