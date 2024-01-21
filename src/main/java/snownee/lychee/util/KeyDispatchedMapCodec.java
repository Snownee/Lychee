package snownee.lychee.util;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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

import snownee.lychee.Lychee;

public final class KeyDispatchedMapCodec<K, V> extends MapCodec<Map<K, V>> {
	private final Codec<K> keyCodec;
	private final Function<? super V, ? extends DataResult<? extends K>> type;
	private final Function<? super K, ? extends DataResult<? extends Decoder<? extends V>>> decoder;
	private final Function<? super V, ? extends DataResult<? extends Encoder<V>>> encoder;
	private final Keyable keys;

	public KeyDispatchedMapCodec(
			final Codec<K> keyCodec,
			final Function<? super V, ? extends DataResult<? extends K>> type,
			final Function<? super K, ? extends DataResult<? extends Decoder<? extends V>>> decoder,
			final Function<? super V, ? extends DataResult<? extends Encoder<V>>> encoder,
			final Keyable keys
	) {
		this.keyCodec = keyCodec;
		this.type = type;
		this.decoder = decoder;
		this.encoder = encoder;
		this.keys = keys;
	}

	/**
	 * Assumes codec(type(V)) is Codec<V>
	 */
	public KeyDispatchedMapCodec(
			final Codec<K> keyCodec,
			final Function<? super V, ? extends DataResult<? extends K>> type,
			final Function<? super K, ? extends DataResult<? extends Codec<? extends V>>> codec,
			final Keyable keys
	) {
		this(keyCodec, type, codec, v -> getCodec(type, codec, v), keys);
	}

	@Override
	public <T> Stream<T> keys(final DynamicOps<T> ops) {
		return keys.keys(ops);
	}

	@Override
	public <T> DataResult<Map<K, V>> decode(final DynamicOps<T> ops, final MapLike<T> input) {
		final var read = ImmutableMap.<K, V>builder();
		final var failed = ImmutableList.<Pair<T, T>>builder();

		final var result = input.entries().reduce(
				DataResult.success(Unit.INSTANCE, Lifecycle.stable()),
				(r, pair) -> {
					final var k = keyCodec.parse(ops, pair.getFirst());
					final var v =
							decoder.apply(k.getOrThrow(
									false,
									(err) -> Lychee.LOGGER.error("Failed get key from {}", pair.getFirst())
							)).getOrThrow(
									false,
									(err) -> Lychee.LOGGER.error("Failed get codec for {}", k.result().get())
							).parse(ops, pair.getSecond());

					final DataResult<Pair<K, V>> entry = k.apply2stable(Pair::of, v);
					entry.error().ifPresent(e -> failed.add(pair));

					return r.apply2stable((u, p) -> {
						read.put(p.getFirst(), p.getSecond());
						return u;
					}, entry);
				},
				(r1, r2) -> r1.apply2stable((u1, u2) -> u1, r2)
		);

		final Map<K, V> elements = read.build();
		final T errors = ops.createMap(failed.build().stream());

		return result.map(unit -> elements).setPartial(elements).mapError(e -> e + " missed input: " + errors);
	}

	@Override
	public <T> RecordBuilder<T> encode(final Map<K, V> input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
		for (final Map.Entry<K, V> entry : input.entrySet()) {
			prefix.add(
					keyCodec.encodeStart(ops, entry.getKey()),
					encoder.apply(entry.getValue())
						   .getOrThrow(false, (err) -> Lychee.LOGGER.error("Failed get codec for {}", entry.getKey()))
						   .encodeStart(ops, entry.getValue())
			);
		}
		return prefix;
	}

	@SuppressWarnings("unchecked")
	private static <K, V> DataResult<? extends Encoder<V>> getCodec(
			final Function<? super V, ? extends DataResult<? extends K>> type,
			final Function<? super K, ? extends DataResult<? extends Encoder<? extends V>>> encoder,
			final V input
	) {
		return type.apply(input)
				   .<Encoder<? extends V>>flatMap(k -> encoder.apply(k).map(Function.identity()))
				   .map(c -> ((Encoder<V>) c));
	}

	@Override
	public String toString() {
		return "KeyDispatchMapCodec[" + keyCodec.toString() + " " + type + " " + decoder + "]";
	}
}
