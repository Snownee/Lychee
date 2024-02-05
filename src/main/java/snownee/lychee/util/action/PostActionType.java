package snownee.lychee.util.action;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ExtraCodecs;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.SerializableType;
import snownee.lychee.util.codec.CompactListCodec;

public interface PostActionType<T extends PostAction<T>> extends SerializableType<T> {
	Codec<PostAction<?>> CODEC = LycheeRegistries.POST_ACTION.byNameCodec().dispatch(
			PostAction::type,
			PostActionType::codec
	);

	Codec<List<PostAction<?>>> LIST_CODEC = new CompactListCodec<>(CODEC);

	RecordCodecBuilder<? extends PostAction<?>, Optional<String>> PATH_CODEC =
			ExtraCodecs.strictOptionalField(Codec.STRING, "path")
					   .forGetter(PostAction::getPath);
}
