package snownee.lychee.util.predicates;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public record BlockPredicate(
		Optional<TagKey<Block>> tag,
		Optional<HolderSet<Block>> blocks,
		Optional<StatePropertiesPredicate> properties,
		Optional<NbtPredicate> nbt
) {
	private static final String NOT_PLAIN = "The block predicate not an id or tag. Can't transform to a plain string";

	private static final Codec<HolderSet<Block>> BLOCKS_CODEC =
			BuiltInRegistries.BLOCK
					.holderByNameCodec()
					.listOf()
					.xmap(HolderSet::direct, holderSet -> holderSet.stream().toList());
	private static final Codec<BlockPredicate> STRING_CODEC = ExtraCodecs.TAG_OR_ELEMENT_ID.flatComapMap(
			it -> {
				if (it.tag()) {
					return Builder.block().of(TagKey.create(Registries.BLOCK, it.id())).build();
				} else {
					return Builder.block().of(BuiltInRegistries.BLOCK.get(it.id())).build();
				}
			},
			it -> {
				if (it.properties.isPresent() || it.nbt.isPresent())
					return DataResult.error(() -> NOT_PLAIN);
				if (it.tag.isPresent())
					return DataResult.success(new ExtraCodecs.TagOrElementLocation(it.tag.get().location(), true));

				if (it.blocks.isPresent() && it.blocks.get().size() == 1)
					return DataResult.success(
							new ExtraCodecs.TagOrElementLocation(
									it.blocks.get().get(0).value().builtInRegistryHolder().key().location(),
									false
							)
					);

				return DataResult.error(() -> NOT_PLAIN);
			}
	);
	private static final Codec<BlockPredicate> RECORD_CODEC = RecordCodecBuilder.create(
			instance ->
					instance.group(
							ExtraCodecs.strictOptionalField(TagKey.codec(Registries.BLOCK), "tag")
									   .forGetter(BlockPredicate::tag),
							ExtraCodecs.strictOptionalField(BLOCKS_CODEC, "blocks")
									   .forGetter(BlockPredicate::blocks),
							ExtraCodecs.strictOptionalField(StatePropertiesPredicate.CODEC, "state")
									   .forGetter(BlockPredicate::properties),
							ExtraCodecs.strictOptionalField(NbtPredicate.CODEC, "nbt")
									   .forGetter(BlockPredicate::nbt)
					).apply(instance, BlockPredicate::new)
	);
	public static final Codec<BlockPredicate> CODEC = Codec.either(STRING_CODEC, RECORD_CODEC).xmap(
			it -> it.map(Function.identity(), Function.identity()),
			it -> {
				if (it.tag.isPresent()) {
					return Either.left(it);
				}
				if (it.blocks.isPresent() && it.blocks.get().size() == 1) {
					return Either.left(it);
				}
				return Either.right(it);
			}
	);

	public boolean matches(ServerLevel level, BlockPos pos) {
		if (!level.isLoaded(pos)) {
			return false;
		}
		return unsafeMatches(level.getBlockState(pos), () -> level.getBlockEntity(pos));
	}

	public boolean unsafeMatches(BlockState state, Supplier<BlockEntity> blockEntitySupplier) {
		if (this.tag.isPresent() && !state.is(this.tag.get())) {
			return false;
		}
		if (this.blocks.isPresent() && !state.is(this.blocks.get())) {
			return false;
		}
		if (this.properties.isPresent() && !this.properties.get().matches(state)) {
			return false;
		}

		if (this.nbt.isPresent()) {
			BlockEntity blockEntity = blockEntitySupplier.get();
			return blockEntity != null && this.nbt.get().matches(blockEntity.saveWithFullMetadata());
		}

		return true;
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	public static class Builder {
		private Optional<HolderSet<Block>> blocks = Optional.empty();
		private Optional<TagKey<Block>> tag = Optional.empty();
		private Optional<StatePropertiesPredicate> properties = Optional.empty();
		private Optional<NbtPredicate> nbt = Optional.empty();

		private Builder() {
		}

		public static BlockPredicate.Builder block() {
			return new BlockPredicate.Builder();
		}

		public BlockPredicate.Builder of(Block... blocks) {
			this.blocks = Optional.of(HolderSet.direct(Block::builtInRegistryHolder, blocks));
			return this;
		}

		public BlockPredicate.Builder of(Collection<Block> collection) {
			this.blocks = Optional.of(HolderSet.direct(Block::builtInRegistryHolder, collection));
			return this;
		}

		public BlockPredicate.Builder of(TagKey<Block> tag) {
			this.tag = Optional.of(tag);
			return this;
		}

		public BlockPredicate.Builder hasNbt(CompoundTag nbt) {
			this.nbt = Optional.of(new NbtPredicate(nbt));
			return this;
		}

		public BlockPredicate.Builder setProperties(StatePropertiesPredicate.Builder builder) {
			this.properties = builder.build();
			return this;
		}

		public BlockPredicate build() {
			return new BlockPredicate(this.tag, this.blocks, this.properties, this.nbt);
		}
	}
}
