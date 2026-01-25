package snownee.lychee.util.render;

import java.util.Objects;
import java.util.function.ToIntFunction;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

/**
 * <a href="https://github.com/Engine-Room/Flywheel/blob/1.21.1/dev/common/src/lib/java/dev/engine_room/flywheel/lib/model/baked/VirtualBlockGetter.java">...</a>
 */
public abstract class VirtualBlockGetter implements BlockAndTintGetter {
	protected final VirtualLightEngine lightEngine;

	public VirtualBlockGetter(ToIntFunction<BlockPos> blockLightFunc, ToIntFunction<BlockPos> skyLightFunc) {
		lightEngine = new VirtualLightEngine(blockLightFunc, skyLightFunc, this);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return getBlockState(pos).getFluidState();
	}

	@Override
	public float getShade(Direction direction, boolean shade) {
		return 1f;
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return lightEngine;
	}

	@Override
	public int getBlockTint(BlockPos pos, ColorResolver color) {
		Biome plainsBiome = Objects.requireNonNull(Minecraft.getInstance().getConnection())
				.registryAccess()
				.lookupOrThrow(Registries.BIOME)
				.getValueOrThrow(Biomes.PLAINS);
		return color.getColor(plainsBiome, pos.getX(), pos.getZ());
	}
}