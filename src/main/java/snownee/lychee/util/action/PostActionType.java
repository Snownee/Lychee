package snownee.lychee.util.action;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ExtraCodecs;
import snownee.lychee.util.SerializableType;
import snownee.lychee.util.contextual.ContextualByConditionsHolder;
import snownee.lychee.util.contextual.ContextualConditionsHolder;

public interface PostActionType<T extends PostAction<T>> extends SerializableType<T> {
	RecordCodecBuilder<? extends PostAction<?>, ContextualConditionsHolder> CONTEXTUAL_CODEC =
			ContextualConditionsHolder.CODEC
					.fieldOf("contextual")
					.orElse(new ContextualConditionsHolder())
					.forGetter(ContextualByConditionsHolder::conditionsHolder);

	RecordCodecBuilder<? extends PostAction<?>, Optional<String>> PATH_CODEC =
			ExtraCodecs.strictOptionalField(Codec.STRING, "path")
					   .forGetter(PostAction::path);
}
