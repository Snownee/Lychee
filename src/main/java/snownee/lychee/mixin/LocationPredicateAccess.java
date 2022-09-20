package snownee.lychee.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.FluidPredicate;
import net.minecraft.advancements.critereon.LightPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;

@Mixin(LocationPredicate.class)
public interface LocationPredicateAccess {

	@Accessor
	MinMaxBounds.Doubles getX();

	@Accessor
	MinMaxBounds.Doubles getY();

	@Accessor
	MinMaxBounds.Doubles getZ();

	@Accessor
	@Nullable
	ResourceKey<Biome> getBiome();

	@Accessor
	@Nullable
	ResourceKey<Structure> getStructure();

	@Accessor
	@Nullable
	ResourceKey<Level> getDimension();

	@Accessor
	@Nullable
	Boolean getSmokey();

	@Accessor
	LightPredicate getLight();

	@Accessor
	BlockPredicate getBlock();

	@Accessor
	FluidPredicate getFluid();

}
