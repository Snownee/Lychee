package snownee.lychee.util.context;

import java.util.HashMap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.DummyContainer;

public class LycheeContext extends HashMap<LycheeContextType<?>, LycheeContextValue<?>> implements DummyContainer {
	public static final Codec<LycheeContext> CODEC =
			Codec.simpleMap(
					LycheeRegistries.CONTEXT.byNameCodec(),

					,
					LycheeRegistries.CONTEXT.byNameCodec().flatXmap(it -> {
						final var id = LycheeRegistries.CONTEXT.getId(it);
						if (it instanceof SerializableContextValue<?> type) {
							return DataResult.success(type.codec());
						} else {
							return DataResult.error(() -> "Context type " + id + " is not serializable");
						}
					}),
					LycheeRegistries.CONTEXT
			);

	public <T extends LycheeContextValue<T>> T get(LycheeContextType<T> type) {
		return (T) super.get(type);
	}
}
