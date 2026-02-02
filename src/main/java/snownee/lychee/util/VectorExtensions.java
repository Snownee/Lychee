package snownee.lychee.util;

import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class VectorExtensions {
	public static final Vector2fc ZERO2F = new Vector2f();
	public static final Vector3fc ZERO3F = new Vector3f();

	@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
	public static final Codec<Vector3fc> CODEC3F = Codec.FLOAT.listOf(2, 3).xmap(
			list -> new Vector3f(list.get(0), list.get(1), list.size() == 3 ? list.get(2) : 0),
			vector -> List.of(vector.x(), vector.y(), vector.z()));

	public static final Codec<Vector2ic> CODEC2I = Codec.INT.listOf(1, 2).xmap(
			list -> new Vector2i(list.getFirst(), list.size() == 1 ? list.getFirst() : list.getLast()),
			vector -> List.of(vector.x(), vector.y()));

	public static final StreamCodec<ByteBuf, Vector2ic> STREAM_CODEC2I = ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list(2)).map(
			list -> new Vector2i(list.getFirst(), list.getLast()),
			vector -> List.of(vector.x(), vector.y()));

	public static final StreamCodec<ByteBuf, Vector2fc> STREAM_CODEC2F = ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list(2)).map(
			list -> new Vector2f(list.getFirst(), list.getLast()),
			vector -> List.of(vector.x(), vector.y()));

	public static Vector2f offset(Vector2fc vector, float x, float y) {
		return new Vector2f(vector.x() + x, vector.y() + y);
	}

	public static Vector2f offset(Vector2fc vector, Vector2f other) {
		return new Vector2f(vector.x() + other.x(), vector.y() + other.y());
	}

	public static Vector2f offsetX(Vector2fc vector, float x) {
		return new Vector2f(vector.x() + x, vector.y());
	}

	public static Vector2f offsetY(Vector2fc vector, float y) {
		return new Vector2f(vector.x(), vector.y() + y);
	}
}
