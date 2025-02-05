package snownee.lychee.random_block_ticking;

import java.util.function.Predicate;

import net.minecraft.world.level.block.state.BlockState;

public interface RandomlyTickable {

	void lychee$setTickable(Predicate<BlockState> randomlyTickable);

	boolean lychee$isTickable(BlockState blockState);
}
