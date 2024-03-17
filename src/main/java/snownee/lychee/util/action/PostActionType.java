package snownee.lychee.util.action;

import java.util.List;

import com.mojang.serialization.Codec;

import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.SerializableType;
import snownee.lychee.util.codec.CompactListCodec;

public interface PostActionType<T extends PostAction> extends SerializableType<T> {
	Codec<PostAction> CODEC = LycheeRegistries.POST_ACTION.byNameCodec().dispatch(
			PostAction::type,
			PostActionType::codec
	);

	Codec<List<PostAction>> LIST_CODEC = new CompactListCodec<>(CODEC);
}
