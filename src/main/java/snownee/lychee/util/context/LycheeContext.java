package snownee.lychee.util.context;

import java.util.HashMap;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.DummyContainer;
import snownee.lychee.util.KeyDispatchedMapCodec;
import snownee.lychee.util.SerializableType;

@SuppressWarnings("unchecked")
public class LycheeContext extends HashMap<LycheeContextType<?>, LycheeContextValue<?>> implements DummyContainer {
	public static final Codec<LycheeContext> CODEC =
			new KeyDispatchedMapCodec<LycheeContextType<?>, LycheeContextValue<?>>(
					LycheeRegistries.CONTEXT.byNameCodec(),
					it -> DataResult.success(it.type()),
					it -> it instanceof SerializableType<?> serializableType
						  ? DataResult.success((Codec<? extends LycheeContextValue<?>>) serializableType.codec())
						  : DataResult.error(() -> it + " isn't serializable"),
					LycheeRegistries.CONTEXT
			).codec().xmap(it -> {
				final var context = new LycheeContext();
				context.putAll(it);
				return context;
			}, Function.identity());

	public <T extends LycheeContextValue<T>> T get(LycheeContextType<T> type) {
		return (T) super.get(type);
	}
}
