package snownee.lychee.util.ui;

import java.util.List;
import java.util.Optional;

import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import snownee.lychee.util.VectorExtensions;

public record UIElementCommonProperties(
		Vector2fc pos,
		int sortOrder,
		Vector2ic size,
		Optional<List<Component>> tooltip,
		Optional<String> onInput,
		float opacity) {
	public static final Vector2ic DEFAULT_SIZE = new Vector2i(16);

	public static final MapCodec<UIElementCommonProperties> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ExtraCodecs.VECTOR2F.optionalFieldOf("pos", VectorExtensions.ZERO2F).forGetter(UIElementCommonProperties::pos),
			Codec.INT.optionalFieldOf("sort_order", 0).forGetter(UIElementCommonProperties::sortOrder),
			VectorExtensions.CODEC2I.optionalFieldOf("size", DEFAULT_SIZE).forGetter(UIElementCommonProperties::size),
			ExtraCodecs.compactListCodec(ComponentSerialization.CODEC)
					.optionalFieldOf("tooltip")
					.forGetter(UIElementCommonProperties::tooltip),
			ExtraCodecs.NON_EMPTY_STRING.optionalFieldOf("on_input").forGetter(UIElementCommonProperties::onInput),
			Codec.floatRange(0, 1).optionalFieldOf("opacity", 1F).forGetter(UIElementCommonProperties::opacity)
	).apply(i, UIElementCommonProperties::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, UIElementCommonProperties> STREAM_CODEC = StreamCodec.composite(
			VectorExtensions.STREAM_CODEC2F,
			UIElementCommonProperties::pos,
			ByteBufCodecs.VAR_INT,
			UIElementCommonProperties::sortOrder,
			VectorExtensions.STREAM_CODEC2I,
			UIElementCommonProperties::size,
			ByteBufCodecs.optional(ComponentSerialization.TRUSTED_STREAM_CODEC.apply(ByteBufCodecs.list())),
			UIElementCommonProperties::tooltip,
			ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
			UIElementCommonProperties::onInput,
			ByteBufCodecs.FLOAT,
			UIElementCommonProperties::opacity,
			UIElementCommonProperties::new);
}
