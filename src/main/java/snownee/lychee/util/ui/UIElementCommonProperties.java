package snownee.lychee.util.ui;

import java.util.List;
import java.util.Optional;

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3fc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import snownee.kiwi.util.codec.KCodecs;
import snownee.lychee.util.VectorExtensions;

public record UIElementCommonProperties(Vector3fc pos, Vector2ic size, Optional<List<Component>> tooltip, float opacity) {
	public static final Vector2ic DEFAULT_SIZE = new Vector2i(16);

	public static final MapCodec<UIElementCommonProperties> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			VectorExtensions.CODEC3F.optionalFieldOf("pos", VectorExtensions.ZERO3F).forGetter(UIElementCommonProperties::pos),
			VectorExtensions.CODEC2I.optionalFieldOf("size", DEFAULT_SIZE).forGetter(UIElementCommonProperties::size),
			KCodecs.compactList(ComponentSerialization.CODEC).optionalFieldOf("tooltip").forGetter(UIElementCommonProperties::tooltip),
			Codec.floatRange(0, 1).optionalFieldOf("opacity", 1F).forGetter(UIElementCommonProperties::opacity)
	).apply(i, UIElementCommonProperties::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, UIElementCommonProperties> STREAM_CODEC = StreamCodec.composite(
			VectorExtensions.STREAM_CODEC3F,
			UIElementCommonProperties::pos,
			VectorExtensions.STREAM_CODEC2I,
			UIElementCommonProperties::size,
			ByteBufCodecs.optional(ComponentSerialization.TRUSTED_STREAM_CODEC.apply(ByteBufCodecs.list())),
			UIElementCommonProperties::tooltip,
			ByteBufCodecs.FLOAT,
			UIElementCommonProperties::opacity,
			UIElementCommonProperties::new);
}
