package snownee.lychee.context;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.util.ExtraCodecs;
import snownee.lychee.util.SerializableType;
import snownee.lychee.util.context.LycheeContextType;
import snownee.lychee.util.context.LycheeContextValue;

// TODO 暂时不涉及
public record JsonContext(JsonObject json) implements LycheeContextValue<JsonContext> {
	@Override
	public LycheeContextType<JsonContext> type() {
		return LycheeContextType.JSON;
	}

	public static final class Type implements LycheeContextType<JsonContext>, SerializableType<JsonContext> {
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
		public Codec<JsonContext> codec() {
			return CODEC;
		}
	}
}
