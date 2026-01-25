package snownee.lychee.util.predicates;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;

import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record BlockStateSet(ImmutableSet<BlockState> blockStates) implements Predicate<BlockState> {
	public static final Predicate<BlockState> ANY = Predicates.alwaysTrue();
	public static final Predicate<BlockState> NONE = Predicates.alwaysFalse();

	public static Predicate<BlockState> of(Block block, Stream<BlockPredicate> stream) {
		List<BlockPredicate> list = stream.toList();
		if (list.isEmpty()) {
			return NONE;
		}
		for (BlockPredicate predicate : list) {
			if (predicate.properties().isEmpty()) {
				return ANY;
			}
		}
		ImmutableSet.Builder<BlockState> builder = ImmutableSet.builder();
		for (BlockState blockState : block.getStateDefinition().getPossibleStates()) {
			for (BlockPredicate predicate : list) {
				if (predicate.properties().orElseThrow().matches(blockState)) {
					builder.add(blockState);
					break;
				}
			}
		}
		ImmutableSet<BlockState> set = builder.build();
		if (set.isEmpty()) {
			return NONE;
		}
		return new BlockStateSet(set);
	}

	@Override
	public boolean test(BlockState blockState) {
		return blockStates.contains(blockState);
	}
}
