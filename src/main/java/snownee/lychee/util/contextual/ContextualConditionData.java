package snownee.lychee.util.contextual;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record ContextualConditionData<T extends ContextualCondition>(
		ContextualCondition condition, boolean secret, Optional<Component> description
) {
	public static final Codec<ContextualConditionData<?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ContextualCondition.CODEC.forGetter(ContextualConditionData::condition),
			Codec.BOOL.optionalFieldOf("secret", false).forGetter(ContextualConditionData::secret),
			ComponentSerialization.CODEC.optionalFieldOf("description").forGetter(ContextualConditionData::description)
	).apply(instance, ContextualConditionData::new));

	public ContextualConditionData(ContextualCondition condition) {
		this(condition, false, Optional.empty());
	}
}
