package snownee.lychee.ui;

import org.joml.Vector3fc;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import snownee.lychee.util.VectorExtensions;

public record GameElementProperties(Vector3fc localPos, Vector3fc rotation, Vector3fc rotationOffset, float scale) {
	public static final MapCodec<GameElementProperties> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			VectorExtensions.CODEC3F.optionalFieldOf("local_pos", VectorExtensions.ZERO3F).forGetter(GameElementProperties::localPos),
			VectorExtensions.CODEC3F.optionalFieldOf("rotation", VectorExtensions.ZERO3F).forGetter(GameElementProperties::rotation),
			VectorExtensions.CODEC3F.optionalFieldOf("rotation_offset", VectorExtensions.ZERO3F)
					.forGetter(GameElementProperties::rotationOffset),
			ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("scale", 1F).forGetter(GameElementProperties::scale)
	).apply(i, GameElementProperties::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, GameElementProperties> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VECTOR3F,
			GameElementProperties::localPos,
			ByteBufCodecs.VECTOR3F,
			GameElementProperties::rotation,
			ByteBufCodecs.VECTOR3F,
			GameElementProperties::rotationOffset,
			ByteBufCodecs.FLOAT,
			GameElementProperties::scale,
			GameElementProperties::new);
}
