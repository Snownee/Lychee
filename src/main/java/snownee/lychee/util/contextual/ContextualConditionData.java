package snownee.lychee.util.contextual;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record ContextualConditionData<T extends ContextualCondition<T>>(
		ContextualCondition<T> condition, boolean secret, Optional<Component> description
) {
	public static final Codec<ContextualConditionData<?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ContextualCondition.CODEC.fieldOf("condition").forGetter(ContextualConditionData::condition),
			Codec.BOOL.optionalFieldOf("secret", false).forGetter(ContextualConditionData::secret),
			ComponentSerialization.CODEC.optionalFieldOf("description").forGetter(ContextualConditionData::description)
	).apply(instance, ContextualConditionData::new));

	public ContextualConditionData(ContextualCondition<T> condition) {
		this(condition, false, Optional.empty());
	}

//	static <T extends ContextualCondition<T>> ContextualConditionData<T> fromNetwork(FriendlyByteBuf buf) {
//		return new ContextualConditionData<>(
//				(T) CommonProxy.readRegistryId(LycheeRegistries.CONTEXTUAL, buf).fromNetwork(buf),
//				buf.readBoolean(),
//				buf.readOptional(FriendlyByteBuf::readComponent)
//		);
//	}
//
//	public void toNetwork(FriendlyByteBuf buf) {
//		CommonProxy.writeRegistryId(LycheeRegistries.CONTEXTUAL, condition.type(), buf);
//		buf.writeBoolean(secret);
//		buf.writeOptional(description, FriendlyByteBuf::writeComponent);
//	}
}
