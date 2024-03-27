package snownee.lychee.util.contextual;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;

public record ContextualConditionData<T extends ContextualCondition>(
		ContextualCondition condition, boolean secret, Optional<Component> description
) {
	public static final Codec<ContextualConditionData<?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ContextualCondition.CODEC.forGetter(ContextualConditionData::condition),
			ExtraCodecs.strictOptionalField(Codec.BOOL, "secret", false).forGetter(ContextualConditionData::secret),
			ExtraCodecs.strictOptionalField(ComponentSerialization.CODEC, "description").forGetter(ContextualConditionData::description)
	).apply(instance, ContextualConditionData::new));

	public ContextualConditionData(ContextualCondition condition) {
		this(condition, false, Optional.empty());
	}
}
