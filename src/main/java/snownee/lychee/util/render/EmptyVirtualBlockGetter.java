package snownee.lychee.util.render;

import java.util.function.ToIntFunction;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * https://github.com/Engine-Room/Flywheel/blob/1.21.1/dev/common/src/lib/java/dev/engine_room/flywheel/lib/model/baked/EmptyVirtualBlockGetter.java
 */
public class EmptyVirtualBlockGetter extends VirtualBlockGetter {
	public static final EmptyVirtualBlockGetter FULL_DARK = new EmptyVirtualBlockGetter(p -> 0, p -> 0);
	public static final EmptyVirtualBlockGetter MID_BRIGHT = new EmptyVirtualBlockGetter(p -> 7, p -> 15);
	public static final EmptyVirtualBlockGetter FULL_BRIGHT = new EmptyVirtualBlockGetter(p -> 15, p -> 15);

	public EmptyVirtualBlockGetter(ToIntFunction<BlockPos> blockLightFunc, ToIntFunction<BlockPos> skyLightFunc) {
		super(blockLightFunc, skyLightFunc);
	}

	@Override
	@Nullable
	public final BlockEntity getBlockEntity(BlockPos pos) {
		return null;
	}

	@Override
	public final BlockState getBlockState(BlockPos pos) {
		return Blocks.AIR.defaultBlockState();
	}

	@Override
	public final FluidState getFluidState(BlockPos pos) {
		return Fluids.EMPTY.defaultFluidState();
	}

	@Override
	public final int getHeight() {
		return 1;
	}

	@Override
	public int getMinY() {
		return 0;
	}
}