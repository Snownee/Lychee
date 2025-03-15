package snownee.lychee.util.codec;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.util.NonNullListExtensions;

public final class LycheeCodecs {
	private static final NonNullList<?> EMPTY_NON_NULL_LIST = NonNullList.create();

	public static final Codec<ItemStack> ITEM_STACK_CODEC = Codec.withAlternative(
			ItemStack.OPTIONAL_CODEC,
			BuiltInRegistries.ITEM.holderByNameCodec().xmap(ItemStack::new, ItemStack::getItemHolder));

	public static final MapCodec<ItemStack> FLAT_ITEM_STACK_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
					BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("id").forGetter(ItemStack::getItemHolder),
					ExtraCodecs.NON_NEGATIVE_INT.fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
					DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch))
			.apply(instance, ItemStack::new));

	public static final MapCodec<BlockPos> OFFSET_CODEC = RecordCodecBuilder.mapCodec(posInstance -> posInstance.group(
			Codec.INT.optionalFieldOf("offsetX", 0).forGetter(Vec3i::getX),
			Codec.INT.optionalFieldOf("offsetY", 0).forGetter(Vec3i::getY),
			Codec.INT.optionalFieldOf("offsetZ", 0).forGetter(Vec3i::getZ)).apply(
			posInstance, (x, y, z) -> {
				if (x == 0 && y == 0 && z == 0) {
					return BlockPos.ZERO;
				}
				return new BlockPos(x, y, z);
			}));

	public static <T> Codec<NonNullList<T>> nonNullList(Codec<T> elementCodec) {
		return Codec.withAlternative(
				NonNullListExtensions.codecOf(elementCodec),
				elementCodec,
				e -> NonNullListExtensions.copyOf(List.of(e)));
	}

	public static <T> Codec<NonNullList<T>> nonNullList(Codec<T> elementCodec, int minSize, int maxSize) {
		return Codec.withAlternative(
				sizeLimit(NonNullListExtensions.codecOf(elementCodec), minSize, maxSize),
				elementCodec,
				e -> NonNullListExtensions.copyOf(List.of(e)));
	}

	public static <T> NonNullList<T> emptyNonNullList() {
		//noinspection unchecked
		return (NonNullList<T>) EMPTY_NON_NULL_LIST;
	}

	public static <T, L extends List<T>> Codec<L> sizeLimit(Codec<L> listCodec, int minSize, int maxSize) {
		return listCodec.validate(list -> {
			if (list.size() < minSize) {
				return DataResult.error(() -> "List is too short: " + minSize + ", expected range [" + minSize + "-" + maxSize + "]");
			}
			if (list.size() > maxSize) {
				return DataResult.error(() -> "List is too long: " + maxSize + ", expected range [" + minSize + "-" + maxSize + "]");
			}
			return DataResult.success(list);
		});
	}

	// https://github.com/neoforged/NeoForge/blob/1.21.x/src/main/java/net/neoforged/neoforge/common/util/NeoForgeExtraCodecs.java#L218

	/**
	 * Map dispatch codec with an alternative.
	 *
	 * <p>The alternative will only be used if there is no {@code "type"} key in the serialized object.
	 *
	 * @param typeCodec     codec for the dispatch type
	 * @param type          function to retrieve the dispatch type from the dispatched type
	 * @param codec         function to retrieve the dispatched type map codec from the dispatch type
	 * @param fallbackCodec fallback to use when the deserialized object does not have a {@code "type"} key
	 * @param <A>           dispatch type
	 * @param <E>           dispatched type
	 * @param <B>           fallback type
	 */
	public static <A, E, B> MapCodec<Either<E, B>> dispatchMapOrElse(
			Codec<A> typeCodec,
			String typeKey,
			Function<? super E, ? extends A> type,
			Function<? super A, ? extends MapCodec<? extends E>> codec,
			MapCodec<B> fallbackCodec) {
		var dispatchCodec = typeCodec.dispatchMap(typeKey, type, codec);
		return new MapCodec<>() {
			@Override
			public <T> Stream<T> keys(DynamicOps<T> ops) {
				return Stream.concat(dispatchCodec.keys(ops), fallbackCodec.keys(ops)).distinct();
			}

			@Override
			public <T> DataResult<Either<E, B>> decode(DynamicOps<T> ops, MapLike<T> input) {
				if (input.get(typeKey) != null) {
					return dispatchCodec.decode(ops, input).map(Either::left);
				} else {
					return fallbackCodec.decode(ops, input).map(Either::right);
				}
			}

			@Override
			public <T> RecordBuilder<T> encode(Either<E, B> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
				return input.map(
						dispatched -> dispatchCodec.encode(dispatched, ops, prefix),
						fallback -> fallbackCodec.encode(fallback, ops, prefix));
			}

			@Override
			public String toString() {
				return "DispatchOrElse[" + dispatchCodec + ", " + fallbackCodec + "]";
			}
		};
	}

	/**
	 * Codec that matches exactly one out of two map codecs.
	 * Same as {@link Codec#xor} but for {@link MapCodec}s.
	 */
	public static <F, S> MapCodec<Either<F, S>> xor(MapCodec<F> first, MapCodec<S> second) {
		return new XorMapCodec<>(first, second);
	}

	private static final class XorMapCodec<F, S> extends MapCodec<Either<F, S>> {
		private final MapCodec<F> first;
		private final MapCodec<S> second;

		private XorMapCodec(MapCodec<F> first, MapCodec<S> second) {
			this.first = first;
			this.second = second;
		}

		@Override
		public <T> Stream<T> keys(DynamicOps<T> ops) {
			return Stream.concat(first.keys(ops), second.keys(ops)).distinct();
		}

		@Override
		public <T> DataResult<Either<F, S>> decode(DynamicOps<T> ops, MapLike<T> input) {
			DataResult<Either<F, S>> firstResult = first.decode(ops, input).map(Either::left);
			DataResult<Either<F, S>> secondResult = second.decode(ops, input).map(Either::right);
			var firstValue = firstResult.result();
			var secondValue = secondResult.result();
			if (firstValue.isPresent() && secondValue.isPresent()) {
				return DataResult.error(
						() -> "Both alternatives read successfully, cannot pick the correct one; first: " + firstValue.get() + " second: "
								+ secondValue.get(),
						firstValue.get());
			} else if (firstValue.isPresent()) {
				return firstResult;
			} else if (secondValue.isPresent()) {
				return secondResult;
			} else {
				return firstResult.apply2((x, y) -> y, secondResult);
			}
		}

		@Override
		public <T> RecordBuilder<T> encode(Either<F, S> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
			return input.map(x -> first.encode(x, ops, prefix), x -> second.encode(x, ops, prefix));
		}

		@Override
		public String toString() {
			return "XorMapCodec[" + first + ", " + second + "]";
		}
	}
}
