package snownee.lychee.util.codec;

import java.util.function.Function;

import com.mojang.datafixers.util.Function7;

import net.minecraft.network.codec.StreamCodec;
import snownee.kiwi.util.NotNullByDefault;

@NotNullByDefault
public interface LycheeStreamCodecs {
	static <B, V> StreamCodec<B, V> uncheckedUnit(V object) {
		return new StreamCodec<>() {
			public V decode(B buf) {
				return object;
			}

			public void encode(B buf, V input) {
			}
		};
	}

	static <B, C, T1, T2, T3, T4, T5, T6, T7> StreamCodec<B, C> composite(
			StreamCodec<? super B, T1> codec1,
			Function<C, T1> getter1,
			StreamCodec<? super B, T2> codec2,
			Function<C, T2> getter2,
			StreamCodec<? super B, T3> codec3,
			Function<C, T3> getter3,
			StreamCodec<? super B, T4> codec4,
			Function<C, T4> getter4,
			StreamCodec<? super B, T5> codec5,
			Function<C, T5> getter5,
			StreamCodec<? super B, T6> codec6,
			Function<C, T6> getter6,
			StreamCodec<? super B, T7> codec7,
			Function<C, T7> getter7,
			Function7<T1, T2, T3, T4, T5, T6, T7, C> factory
	) {
		return new StreamCodec<>() {
			public C decode(B object) {
				T1 object1 = codec1.decode(object);
				T2 object2 = codec2.decode(object);
				T3 object3 = codec3.decode(object);
				T4 object4 = codec4.decode(object);
				T5 object5 = codec5.decode(object);
				T6 object6 = codec6.decode(object);
				T7 object7 = codec7.decode(object);
				return factory.apply(object1, object2, object3, object4, object5, object6, object7);
			}

			public void encode(B object, C object2) {
				codec1.encode(object, getter1.apply(object2));
				codec2.encode(object, getter2.apply(object2));
				codec3.encode(object, getter3.apply(object2));
				codec4.encode(object, getter4.apply(object2));
				codec5.encode(object, getter5.apply(object2));
				codec6.encode(object, getter6.apply(object2));
				codec7.encode(object, getter7.apply(object2));
			}
		};
	}
}
