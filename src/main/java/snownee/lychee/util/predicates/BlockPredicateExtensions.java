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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
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
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;

public class BlockPredicateExtensions {
	public static final BlockPredicate ANY = new BlockPredicate(Optional.empty(), Optional.empty(), Optional.empty());
	private static final Cache<BlockPredicate, List<BlockState>> CACHE = CacheBuilder.newBuilder()
			.expireAfterAccess(10, TimeUnit.MINUTES)
			.build();
	public static final Set<Property<?>> ITERABLE_PROPERTIES = Sets.newConcurrentHashSet(List.of(
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

	public static final Codec<BlockPredicate> CODEC_FOR_TESTING = codec(true);

	private static <T extends Comparable<T>> String getNameByValue(Property<T> property, Object value) {
		//noinspection unchecked
		return property.getName((T) value);
	}
	public static final Codec<BlockPredicate> CODEC = codec(false);

	public static <T> DataResult<BlockPredicate> fromString(DynamicOps<T> ops, T input, boolean forTesting) {
		String s = ops.getStringValue(input).result().orElseThrow();
		if ("*".equals(s)) {
			return DataResult.success(ANY);
		}
		Either<BlockStateParser.BlockResult, BlockStateParser.TagResult> result;
		try {
			if (forTesting) {
				result = BlockStateParser.parseForTesting(BuiltInRegistries.BLOCK.asLookup(), s, true);
			} else {
				result = Either.left(BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), s, true));
			}
		} catch (Exception e) {
			return DataResult.error(() -> "Invalid block predicate: %s - %s".formatted(s, e.getMessage()));
		}

		return DataResult.success(result.map(
				$ -> new BlockPredicate(
						Optional.of(HolderSet.direct($.blockState().getBlockHolder())),
						$.properties().isEmpty() ?
								Optional.empty() :
								Optional.of(new StatePropertiesPredicate($.properties()
										.entrySet()
										.stream()
										.map(it -> new StatePropertiesPredicate.PropertyMatcher(
												it.getKey().getName(),
												new StatePropertiesPredicate.ExactMatcher(getNameByValue(it.getKey(), it.getValue()))))
										.toList())),
						Optional.ofNullable($.nbt()).map(NbtPredicate::new)),
				$ -> new BlockPredicate(
						Optional.of($.tag()),
						$.vagueProperties().isEmpty() ?
								Optional.empty() :
								Optional.of(new StatePropertiesPredicate($.vagueProperties()
										.entrySet()
										.stream()
										.map(it -> new StatePropertiesPredicate.PropertyMatcher(
												it.getKey(),
												new StatePropertiesPredicate.ExactMatcher(it.getValue())))
										.toList())),
						Optional.ofNullable($.nbt()).map(NbtPredicate::new)
				)
		));
	}

	private static Codec<BlockPredicate> codec(boolean forTesting) {
		return Codec.of(
				BlockPredicate.CODEC, new Decoder<>() {
					@Override
					public <T> DataResult<Pair<BlockPredicate, T>> decode(DynamicOps<T> ops, T input) {
						var stringValue = ops.getStringValue(input);
						if (stringValue.result().isPresent()) {
							return fromString(stringValue.getOrThrow(), forTesting).flatMap(it -> DataResult.success(Pair.of(
									it,
									ops.empty())));
						}
						DataResult<Pair<BlockPredicate, T>> result = BlockPredicate.CODEC.decode(ops, input);
						if (result.result().isPresent() && isAny(result.getOrThrow().getFirst())) {
							return DataResult.error(() -> "Wildcard BlockPredicate must be \"*\" string, but found " + input);
						}
						return result;
					}
				});
	}

	public static boolean isAny(BlockPredicate predicate) {
		if (predicate == ANY) {
			return true;
		}
		return predicate.blocks().isEmpty() && predicate.properties().isEmpty() && predicate.nbt().isEmpty();
	}

	public static Set<Block> matchedBlocks(BlockPredicate predicate) {
		if (isAny(predicate)) {
			return Set.of();
		}
		final var blocks = Lists.<Holder<Block>>newArrayList();
		if (predicate.blocks().isPresent()) {
			Iterables.addAll(
					blocks,
					predicate.blocks().get().unwrap().map(BuiltInRegistries.BLOCK::getOrCreateTag, Function.identity()));
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
		if (isAny(predicate)) {
			return List.of();
		}
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
	public static boolean matches(BlockPredicate predicate, LycheeContext context) {
		final var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
		return unsafeMatches(
				context.level(),
				predicate,
				lootParamsContext.get(LootContextParams.BLOCK_STATE),
				() -> lootParamsContext.getOrNull(LootContextParams.BLOCK_ENTITY)
		);
	}

	public static boolean unsafeMatches(
			Level level,
			BlockPredicate predicate,
			BlockState state,
			Supplier<BlockEntity> blockEntitySupplier
	) {
		if (predicate.blocks().isPresent() && !state.is(predicate.blocks().get())) {
			return false;
		}
		if (predicate.properties().isPresent() && !predicate.properties().get().matches(state)) {
			return false;
		}

		if (predicate.nbt().isPresent()) {
			final var blockEntity = blockEntitySupplier.get();
			return blockEntity != null && predicate.nbt().get().matches(blockEntity.saveWithFullMetadata(level.registryAccess()));
		}

		return true;
	}

	public static BlockState anyBlockState(BlockPredicate predicate) {
		return getShowcaseBlockStates(predicate).stream().findFirst().orElse(Blocks.AIR.defaultBlockState());
	}

	public static List<BlockState> getShowcaseBlockStates(BlockPredicate predicate) {
		if (isAny(predicate)) {
			return List.of();
		}
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
		if (blocks.isEmpty()) {
			return List.of();
		}

		final var states = Lists.<BlockState>newArrayList();

		for (final var block : blocks) {
			final var state = block.defaultBlockState();
			final var propertyMap = ArrayListMultimap.<Property<?>, Comparable<?>>create();
			for (var property : block.getStateDefinition().getProperties()) {
				final var name = property.getName();
				final var matcher = predicate.properties().flatMap(it -> PropertiesPredicateExtensions.findMatcher(it, name));
				if (matcher.isPresent()) {
					for (Comparable<?> object : property.getPossibleValues()) {
						if (matcher.get().match(
								block.getStateDefinition(),
								state.setValue((Property) property, (Comparable) object)
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
								.map(v -> $.setValue((Property) e.getKey(), (Comparable) v))
				);
			}

			states.addAll(stream.toList());
		}
		return states;
	}

	@SuppressWarnings("rawtypes")
	public static List<Component> getTooltips(BlockState state, BlockPredicate predicate) {
		if (isAny(predicate)) {
			return List.of();
		}
		final var list = Lists.<Component>newArrayList(state.getBlock().getName());
		final var matchers = predicate.properties().map(StatePropertiesPredicate::properties);
		if (matchers.isPresent()) {
			for (final var matcher : matchers.get()) {
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
					var min = rangedMatcher.minValue().orElseGet(() -> property.getName((Comparable) rangePair.get().getFirst()));
					var max = rangedMatcher.maxValue().orElseGet(() -> property.getName((Comparable) rangePair.get().getSecond()));
					name.append(Component.literal(min).withStyle(ChatFormatting.WHITE));
					if (!min.equals(max)) {
						name.append(Component.literal("~").withStyle(ChatFormatting.GRAY));
						name.append(Component.literal(max).withStyle(ChatFormatting.WHITE));
					}
				}
				list.add(name);
			}
		}
		if (predicate.nbt().isPresent()) {
			list.add(Component.translatable("tip.lychee.nbtPredicate").withStyle(ChatFormatting.GRAY));
		}
		return list;
	}

}