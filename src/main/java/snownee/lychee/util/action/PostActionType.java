package snownee.lychee.util.action;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ExtraCodecs;
import snownee.lychee.util.SerializableType;
import snownee.lychee.util.contextual.ContextualByCommonHolder;
import snownee.lychee.util.contextual.ContextualCommonHolder;

public interface PostActionType<T extends PostAction<T>> extends SerializableType<T> {
	RecordCodecBuilder<? extends PostAction<?>, ContextualCommonHolder> CONTEXTUAL_CODEC =
			ContextualCommonHolder.CODEC
					.fieldOf("contextual")
					.orElse(new ContextualCommonHolder())
					.forGetter(ContextualByCommonHolder::contextualCommonHolder);

	RecordCodecBuilder<? extends PostAction<?>, Optional<String>> PATH_CODEC =
			ExtraCodecs.strictOptionalField(Codec.STRING, "path")
					   .forGetter(PostAction::getPath);
}
