package snownee.lychee.util.predicates;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.FluidPredicate;
import net.minecraft.advancements.critereon.LightPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.levelgen.structure.Structure;
import snownee.lychee.mixin.predicates.PositionPredicateAccess;
import snownee.lychee.util.TagOrElementHolder;

/**
 * This class using {@link TagOrElementHolder} for fields
 */
public record LocationPredicate(
		Optional<net.minecraft.advancements.critereon.LocationPredicate.PositionPredicate> position,
		Optional<TagOrElementHolder<Biome>> biome,
		Optional<TagOrElementHolder<Structure>> structure,
		Optional<TagOrElementHolder<Level>> dimension,
		Optional<Boolean> smokey,
		Optional<LightPredicate> light,
		Optional<BlockPredicate> block,
		Optional<FluidPredicate> fluid
) {

	public static final Codec<LocationPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ExtraCodecs.strictOptionalField(
					net.minecraft.advancements.critereon.LocationPredicate.PositionPredicate.CODEC,
					"position"
			).forGetter(LocationPredicate::position),
			ExtraCodecs.strictOptionalField(TagOrElementHolder.<Biome>codec(), "biome")
					.forGetter(LocationPredicate::biome),
			ExtraCodecs.strictOptionalField(TagOrElementHolder.<Structure>codec(), "structure")
					.forGetter(LocationPredicate::structure),
			ExtraCodecs.strictOptionalField(TagOrElementHolder.<Level>codec(), "dimension")
					.forGetter(LocationPredicate::dimension),
			ExtraCodecs.strictOptionalField(Codec.BOOL, "smokey").forGetter(LocationPredicate::smokey),
			ExtraCodecs.strictOptionalField(LightPredicate.CODEC, "light").forGetter(LocationPredicate::light),
			ExtraCodecs.strictOptionalField(BlockPredicateExtensions.CODEC, "block").forGetter(LocationPredicate::block),
			ExtraCodecs.strictOptionalField(FluidPredicate.CODEC, "fluid").forGetter(LocationPredicate::fluid)
	).apply(instance, LocationPredicate::new));

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public static Optional<LocationPredicate> of(
			Optional<net.minecraft.advancements.critereon.LocationPredicate.PositionPredicate> position,
			Optional<TagOrElementHolder<Biome>> biome,
			Optional<TagOrElementHolder<Structure>> structure,
			Optional<TagOrElementHolder<Level>> dimension,
			Optional<Boolean> smokey,
			Optional<LightPredicate> light,
			Optional<BlockPredicate> block,
			Optional<FluidPredicate> fluid
	) {
		return position.isEmpty() && biome.isEmpty() && structure.isEmpty() && dimension.isEmpty() &&
				smokey.isEmpty() && light.isEmpty() && block.isEmpty() && fluid.isEmpty()
				? Optional.empty()
				: Optional.of(new LocationPredicate(
				position,
				biome,
				structure,
				dimension,
				smokey,
				light,
				block,
				fluid
		));
	}

	public boolean matches(ServerLevel level, double x, double y, double z) {
		if (position.isPresent() && !position.get().matches(x, y, z)) {
			return false;
		}
		if (dimension.isPresent() && !dimension.get().matches(level.registryAccess().registryOrThrow(Registries.DIMENSION), level)) {
			return false;
		}
		BlockPos blockPos = BlockPos.containing(x, y, z);
		boolean posLoaded = level.isLoaded(blockPos);
		if (posLoaded) {
			if (biome.isPresent() && !biome.get().matches(level.registryAccess().registryOrThrow(Registries.BIOME), level.getBiome(blockPos))) {
				return false;
			}
			if (structure.isPresent() && structure.map(it -> {
				if (it.tag()) {
					return !level.structureManager().getStructureWithPieceAt(blockPos, TagKey.create(Registries.STRUCTURE, it.id())).isValid();
				} else {
					return !level.structureManager().getStructureWithPieceAt(blockPos, ResourceKey.create(Registries.STRUCTURE, it.id())).isValid();
				}
			}).get()) {
				return false;
			}
			if (smokey.isPresent() && smokey.get() != CampfireBlock.isSmokeyPos(level, blockPos)) {
				return false;
			}
		}
		if (light.isPresent() && !light.get().matches(level, blockPos)) {
			return false;
		}
		if (block.isPresent() && !block.get().matches(level, blockPos)) {
			return false;
		}
		if (fluid.isPresent() && !fluid.get().matches(level, blockPos)) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public static class Builder {
		private MinMaxBounds.Doubles x = MinMaxBounds.Doubles.ANY;
		private MinMaxBounds.Doubles y = MinMaxBounds.Doubles.ANY;
		private MinMaxBounds.Doubles z = MinMaxBounds.Doubles.ANY;
		Optional<TagOrElementHolder<Biome>> biome;
		Optional<TagOrElementHolder<Structure>> structure;
		Optional<TagOrElementHolder<Level>> dimension;
		private Optional<Boolean> smokey = Optional.empty();
		private Optional<LightPredicate> light = Optional.empty();
		private Optional<BlockPredicate> block = Optional.empty();
		private Optional<FluidPredicate> fluid = Optional.empty();

		public static LocationPredicate.Builder location() {
			return new LocationPredicate.Builder();
		}

		public static LocationPredicate.Builder inBiome(ResourceKey<Biome> biome) {
			return location().setBiome(biome);
		}

		public static LocationPredicate.Builder inBiome(TagKey<Biome> biome) {
			return location().setBiome(biome);
		}

		public static LocationPredicate.Builder inDimension(ResourceKey<Level> dimension) {
			return location().setDimension(dimension);
		}

		public static LocationPredicate.Builder inDimension(TagKey<Level> dimension) {
			return location().setDimension(dimension);
		}

		public static LocationPredicate.Builder inStructure(ResourceKey<Structure> structure) {
			return location().setStructure(structure);
		}

		public static LocationPredicate.Builder inStructure(TagKey<Structure> structure) {
			return location().setStructure(structure);
		}

		public static LocationPredicate.Builder atYLocation(MinMaxBounds.Doubles y) {
			return location().setY(y);
		}

		public LocationPredicate.Builder setX(MinMaxBounds.Doubles x) {
			this.x = x;
			return this;
		}

		public LocationPredicate.Builder setY(MinMaxBounds.Doubles y) {
			this.y = y;
			return this;
		}

		public LocationPredicate.Builder setZ(MinMaxBounds.Doubles z) {
			this.z = z;
			return this;
		}

		public LocationPredicate.Builder setBiome(ResourceKey<Biome> biome) {
			this.biome = Optional.of(new TagOrElementHolder<>(biome.location(), false));
			return this;
		}


		public LocationPredicate.Builder setBiome(TagKey<Biome> biome) {
			this.biome = Optional.of(new TagOrElementHolder<>(biome.location(), true));
			return this;
		}

		public LocationPredicate.Builder setStructure(ResourceKey<Structure> structure) {
			this.structure = Optional.of(new TagOrElementHolder<>(structure.location(), false));
			return this;
		}

		public LocationPredicate.Builder setStructure(TagKey<Structure> structure) {
			this.structure = Optional.of(new TagOrElementHolder<>(structure.location(), true));
			return this;
		}

		public LocationPredicate.Builder setDimension(ResourceKey<Level> dimension) {
			this.dimension = Optional.of(new TagOrElementHolder<>(dimension.location(), false));
			return this;
		}

		public LocationPredicate.Builder setDimension(TagKey<Level> dimension) {
			this.dimension = Optional.of(new TagOrElementHolder<>(dimension.location(), true));
			return this;
		}

		public LocationPredicate.Builder setLight(LightPredicate.Builder light) {
			this.light = Optional.of(light.build());
			return this;
		}

		public LocationPredicate.Builder setBlock(BlockPredicate.Builder block) {
			this.block = Optional.of(block.build());
			return this;
		}

		public LocationPredicate.Builder setFluid(FluidPredicate.Builder fluid) {
			this.fluid = Optional.of(fluid.build());
			return this;
		}

		public LocationPredicate.Builder setSmokey(boolean smokey) {
			this.smokey = Optional.of(smokey);
			return this;
		}

		public LocationPredicate build() {
			return new LocationPredicate(
					PositionPredicateAccess.of(x, y, z),
					biome,
					structure,
					dimension,
					smokey,
					light,
					block,
					fluid
			);
		}
	}
}
