package snownee.lychee.context;

import org.jetbrains.annotations.NotNull;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.util.ExtraCodecs;
import snownee.lychee.util.SerializableType;
import snownee.lychee.util.context.KeyedContextValue;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.context.LycheeContextSerializer;

// Unused for now
public record JsonContext(JsonObject json) implements KeyedContextValue<JsonContext> {
	@Override
	public LycheeContextKey<JsonContext> key() {
		return LycheeContextKey.JSON;
	}

	public static final class Serializer implements LycheeContextSerializer<JsonContext>,
			SerializableType<JsonContext> {
		public static final Codec<JsonContext> CODEC = ExtraCodecs.JSON.comapFlatMap(
				it -> {
					try {
						return DataResult.success(new JsonContext(it.getAsJsonObject()));
					} catch (final Exception e) {
						return DataResult.error(e::getMessage);
					}
				},
				JsonContext::json
		);

		@Override
		public @NotNull Codec<JsonContext> codec() {
			return CODEC;
		}
	}
}
