package snownee.lychee.util.codec;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

public final class KeyDispatchedMapMapCodec<K, V> extends MapCodec<Map<K, V>> {
	private final Codec<K> keyCodec;
	private final Function<? super K, ? extends DataResult<? extends Decoder<? extends V>>> decoder;
	private final Function<? super K, ? extends DataResult<? extends Encoder<V>>> encoder;
	private final Keyable keys;
	private final Supplier<Map<K, V>> mapFactory;

	public KeyDispatchedMapMapCodec(
			final Codec<K> keyCodec,
			final Function<? super K, ? extends DataResult<? extends Decoder<? extends V>>> decoder,
			final Function<? super K, ? extends DataResult<? extends Encoder<V>>> encoder,
			final Keyable keys,
			final Supplier<Map<K, V>> mapFactory) {
		this.keyCodec = keyCodec;
		this.decoder = decoder;
		this.encoder = encoder;
		this.keys = keys;
		this.mapFactory = mapFactory;
	}

	public KeyDispatchedMapMapCodec(
			final Codec<K> keyCodec,
			final Function<? super K, DataResult<Codec<V>>> codec,
			final Keyable keys,
			final Supplier<Map<K, V>> mapFactory) {
		this(keyCodec, codec, codec, keys, mapFactory);
	}

	@Override
	public <T> Stream<T> keys(final DynamicOps<T> ops) {
		return keys.keys(ops);
	}

	@SuppressWarnings("unchecked")
	private static <K, V> DataResult<? extends Encoder<V>> getCodec(
			final Function<? super V, ? extends DataResult<? extends K>> type,
			final Function<? super K, ? extends DataResult<? extends Encoder<? extends V>>> encoder,
			final V input) {
		return type.apply(input).<Encoder<? extends V>>flatMap(k -> encoder.apply(k).map(Function.identity())).map(c -> ((Encoder<V>) c));
	}

	@Override
	public <T> DataResult<Map<K, V>> decode(final DynamicOps<T> ops, final MapLike<T> input) {
		final var elements = mapFactory.get();
		final var failed = ImmutableList.<T>builder();

		final var result = keys(ops).reduce(
				DataResult.success(Unit.INSTANCE, Lifecycle.stable()), (r, key) -> {
					final var parsedKey = keyCodec.parse(ops, key);
					var rawValue = input.get(key);
					// No key in the map
					if (rawValue == null) {
						return r;
					}
					final var valueResult =
							decoder.apply(parsedKey.getOrThrow((err) -> new IllegalStateException(String.format(
											"Failed get key from %s",
											key))))
									.getOrThrow((err) -> new IllegalStateException(String.format("Failed get codec for %s", rawValue)))
									.parse(ops, rawValue);

					final var entry = parsedKey.apply2stable(Pair::of, valueResult);
					entry.error().ifPresent(e -> failed.add(key));

					return r.apply2stable(
							(u, p) -> {
								elements.put(p.getFirst(), p.getSecond());
								return u;
							}, entry);
				}, (r1, r2) -> r1.apply2stable((u1, u2) -> u1, r2));

		final T errors = ops.createList(failed.build().stream());

		return result.map(unit -> elements).setPartial(elements).mapError(e -> e + " missed input: " + errors);
	}

	@Override
	public <T> RecordBuilder<T> encode(final Map<K, V> input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
		keys.keys(ops).forEach(key -> {
			final var parsedKeyResult = keyCodec.parse(ops, key);
			var parsedKey = parsedKeyResult.getOrThrow((err) -> new IllegalStateException(String.format("Failed get key from %s", key)));
			if (!input.containsKey(parsedKey)) {
				return;
			}
			var value = input.get(parsedKey);
			prefix.add(
					key,
					encoder.apply(parsedKey)
							.getOrThrow((err) -> new IllegalStateException(String.format("Failed get codec for %s", key)))
							.encodeStart(ops, value));
		});
		return prefix;
	}

	@Override
	public String toString() {
		return "KeyDispatchMapCodec[" + keyCodec + " " + decoder + "]";
	}
}
