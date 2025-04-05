package snownee.lychee.util.ui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public record UIElementCommonProperties(float x, float y, float z, int width, int height) {
	public static final int DEFAULT_SIZE = 16;

	public static final MapCodec<UIElementCommonProperties> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.FLOAT.optionalFieldOf("x", 0F).forGetter(UIElementCommonProperties::x),
			Codec.FLOAT.optionalFieldOf("y", 0F).forGetter(UIElementCommonProperties::y),
			Codec.FLOAT.optionalFieldOf("z", 0F).forGetter(UIElementCommonProperties::z),
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("width", DEFAULT_SIZE).forGetter(UIElementCommonProperties::width),
			ExtraCodecs.POSITIVE_INT.optionalFieldOf("height", DEFAULT_SIZE).forGetter(UIElementCommonProperties::height)
	).apply(i, UIElementCommonProperties::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, UIElementCommonProperties> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.FLOAT,
			UIElementCommonProperties::x,
			ByteBufCodecs.FLOAT,
			UIElementCommonProperties::y,
			ByteBufCodecs.FLOAT,
			UIElementCommonProperties::z,
			ByteBufCodecs.INT,
			UIElementCommonProperties::width,
			ByteBufCodecs.INT,
			UIElementCommonProperties::height,
			UIElementCommonProperties::new);
}
