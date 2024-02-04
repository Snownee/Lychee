package snownee.lychee.util.predicates;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.mixin.predicates.BlockPredicateAccess;
import snownee.lychee.util.Pair;

public class BlockPredicateExtensions {
	private static final Cache<BlockPredicate, List<BlockState>> CACHE =
			CacheBuilder.newBuilder()
						.expireAfterAccess(10, TimeUnit.MINUTES)
						.build();
	public static Set<Property<?>> ITERABLE_PROPERTIES = Sets.newConcurrentHashSet();

	private static final String NOT_PLAIN = "The block predicate not an id or tag. Can't transform to a plain string";

	private static final Codec<BlockPredicate> STRING_CODEC = ExtraCodecs.TAG_OR_ELEMENT_ID.flatComapMap(
			it -> {
				if (it.tag()) {
					return BlockPredicate.Builder.block().of(TagKey.create(Registries.BLOCK, it.id())).build();
				} else {
					return BlockPredicate.Builder.block().of(BuiltInRegistries.BLOCK.get(it.id())).build();
				}
			},
			it -> {
				if (it.properties().isPresent() || it.nbt().isPresent())
					return DataResult.error(() -> NOT_PLAIN);
				if (it.tag().isPresent())
					return DataResult.success(new ExtraCodecs.TagOrElementLocation(it.tag().get().location(), true));

				if (it.blocks().isPresent() && it.blocks().get().size() == 1)
					return DataResult.success(
							new ExtraCodecs.TagOrElementLocation(
									it.blocks().get().get(0).value().builtInRegistryHolder().key().location(),
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
							ExtraCodecs.strictOptionalField(BlockPredicateAccess.blocksCodec(), "blocks")
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
				if (it.tag().isPresent()) {
					return Either.left(it);
				}
				if (it.blocks().isPresent() && it.blocks().get().size() == 1) {
					return Either.left(it);
				}
				return Either.right(it);
			}
	);

	static {
		ITERABLE_PROPERTIES.addAll(List.of(
				BlockStateProperties.AGE_1,
				BlockStateProperties.AGE_2,
				BlockStateProperties.AGE_3,
				BlockStateProperties.AGE_5,
				BlockStateProperties.AGE_7,
				BlockStateProperties.CANDLES,
				BlockStateProperties.BITES,
				BlockStateProperties.POWER,
				BlockStateProperties.POWERED,
				BlockStateProperties.LIT,
				BlockStateProperties.BERRIES,
				BlockStateProperties.OPEN,
				BlockStateProperties.DELAY,
				BlockStateProperties.DISTANCE,
				BlockStateProperties.LAYERS,
				BlockStateProperties.PICKLES,
				BlockStateProperties.LEVEL,
				BlockStateProperties.LEVEL_HONEY,
				BlockStateProperties.LEVEL_CAULDRON,
				BlockStateProperties.DRIPSTONE_THICKNESS
		));
	}

	public static Set<Block> matchedBlocks(BlockPredicate predicate) {
		final var blocks = Lists.<Holder<Block>>newArrayList();
		if (predicate.blocks().isPresent()) {
			blocks.addAll(predicate.blocks().orElseThrow().unwrap().swap().orThrow());
		}
		if (predicate.tag().isPresent()) {
			blocks.addAll(BuiltInRegistries.BLOCK.getOrCreateTag(predicate.tag().orElseThrow()).stream().toList());
		}
		return blocks.stream().map(Holder::value).collect(Collectors.toSet());
	}

	public static Set<Fluid> matchedFluids(BlockPredicate predicate) {
		/* off */
		return matchedBlocks(predicate).stream()
									   .filter(LiquidBlock.class::isInstance)
									   .map(it -> it.defaultBlockState().getFluidState())
									   .filter(Predicate.not(FluidState::isEmpty))
									   .map(FluidState::getType)
									   .collect(Collectors.toSet());
		/* on */
	}

	public static List<ItemStack> matchedItemStacks(BlockPredicate predicate) {
		return matchedBlocks(predicate).stream()
									   .map(Block::asItem)
									   .filter(Predicate.not(Items.AIR::equals))
									   .distinct()
									   .map(Item::getDefaultInstance)
									   .toList();
	}

	/**
	 * Optimized without get block state and block entity calls. And needn't pos loaded.
	 */
	public static boolean matches(BlockPredicate predicate, LycheeRecipeContext context) {
		return unsafeMatches(
				predicate,
				context.get(LootContextParams.BLOCK_STATE),
				() -> context.getOrNull(LootContextParams.BLOCK_ENTITY)
		);
	}

	public static boolean unsafeMatches(
			BlockPredicate predicate,
			BlockState state,
			Supplier<BlockEntity> blockEntitySupplier
	) {
		if (predicate.tag().isPresent() && !state.is(predicate.tag().get())) {
			return false;
		}
		if (predicate.blocks().isPresent() && !state.is(predicate.blocks().get())) {
			return false;
		}
		if (predicate.properties().isPresent() && !predicate.properties().get().matches(state)) {
			return false;
		}

		if (predicate.nbt().isPresent()) {
			final var blockEntity = blockEntitySupplier.get();
			return blockEntity != null && predicate.nbt().get().matches(blockEntity.saveWithFullMetadata());
		}

		return true;
	}

	public static Optional<BlockPredicate> fromNetwork(FriendlyByteBuf buf) {
		return buf.readOptional((it) -> it.readWithCodecTrusted(NbtOps.INSTANCE, BlockPredicate.CODEC));
	}

	public static void toNetwork(Optional<BlockPredicate> predicate, FriendlyByteBuf buf) {
		buf.writeOptional(predicate, (it, obj) -> it.writeWithCodec(NbtOps.INSTANCE, BlockPredicate.CODEC, obj));
	}

	public static BlockState anyBlockState(BlockPredicate predicate) {
		return getShowcaseBlockStates(predicate).stream().findFirst().orElse(Blocks.AIR.defaultBlockState());
	}

	public static List<BlockState> getShowcaseBlockStates(BlockPredicate predicate) {
		try {
			return CACHE.get(predicate, () -> getShowcaseBlockStates(predicate, ITERABLE_PROPERTIES));
		} catch (ExecutionException e) {
			return List.of();
		}
	}

	public static List<BlockState> getShowcaseBlockStates(
			BlockPredicate predicate,
			Collection<Property<?>> iterableProperties
	) {
		final var blocks = matchedBlocks(predicate);
		if (blocks.isEmpty()) return List.of();

		final var states = Lists.<BlockState>newArrayList();
		final var propertiesPredicate = predicate.properties().orElseThrow();

		for (final var block : blocks) {
			final var state = block.defaultBlockState();
			final var propertyMap = ArrayListMultimap.<Property<?>, Comparable<?>>create();
			for (Property<? extends Comparable<?>> property : block.getStateDefinition().getProperties()) {
				final var name = property.getName();
				final var matcher = PropertiesPredicateExtensions.findMatcher(propertiesPredicate, name);
				if (matcher.isPresent()) {
					for (Comparable<?> object : property.getPossibleValues()) {
						if (matcher.get().match(
								block.getStateDefinition(),
								state.setValue((Property<?>) property, (Comparable) object)
						)) {
							propertyMap.put(property, object);
						}
					}
				} else if (iterableProperties.contains(property)) {
					propertyMap.putAll(property, property.getPossibleValues());
				}
			}
			var stream = Stream.of(state);
			for (final var e : propertyMap.asMap().entrySet()) {
				stream = stream.flatMap(
						$ -> e.getValue()
							  .stream()
							  .map(v -> $.setValue((Property<?>) e.getKey(), (Comparable) v))
				);
			}

			states.addAll(stream.toList());
		}
		return states;
	}

	@SuppressWarnings("rawtypes")
	public static List<Component> getTooltips(BlockState state, BlockPredicate predicate) {
		final var list = Lists.<Component>newArrayList(state.getBlock().getName());
		final var matchers = predicate.properties().orElseThrow().properties();
		for (final var matcher : matchers) {
			final var name = Component.literal(matcher.name() + "=").withStyle(ChatFormatting.GRAY);
			if (matcher.valueMatcher() instanceof StatePropertiesPredicate.ExactMatcher exactMatcher) {
				name.append(Component.literal(exactMatcher.value()).withStyle(ChatFormatting.WHITE));
			} else if (matcher.valueMatcher() instanceof StatePropertiesPredicate.RangedMatcher rangedMatcher) {
				final var definition = state.getBlock().getStateDefinition();
				final Property property = definition.getProperty(matcher.name());
				final var rangePair = Suppliers.memoize(() -> {
					final var sorted = property.getPossibleValues()
											   .stream()
											   .sorted()
											   .toList();
					return Pair.of(sorted.get(0), sorted.get(sorted.size() - 1));
				});
				name.append(Component.literal(rangedMatcher.minValue().orElseGet(
						() -> property.getName((Comparable) rangePair.get().getFirst())
				)).withStyle(ChatFormatting.WHITE));
				name.append(Component.literal("~").withStyle(ChatFormatting.GRAY));
				name.append(Component.literal(rangedMatcher.minValue().orElseGet(
						() -> property.getName((Comparable) rangePair.get().getSecond())
				)).withStyle(ChatFormatting.WHITE));
			}
			list.add(name);
		}
		if (predicate.nbt().isPresent()) {
			list.add(Component.translatable("tip.lychee.nbtPredicate").withStyle(ChatFormatting.GRAY));
		}
		return list;
	}

}
