package snownee.lychee.util.contextual;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.util.CommonProxy;

public record ConditionHolder<T extends ContextualCondition<T>>(
		ContextualCondition<T> condition, boolean secret, Optional<Component> description
) {
	public static final Codec<ConditionHolder<?>> CODEC =
			RecordCodecBuilder.create(instance ->
					instance.group(
							ContextualCondition.CODEC.fieldOf("condition").forGetter(ConditionHolder::condition),
							Codec.BOOL.optionalFieldOf("secret", false).forGetter(ConditionHolder::secret),
							ComponentSerialization.CODEC.optionalFieldOf("description")
														.forGetter(ConditionHolder::description)
					).apply(instance, ConditionHolder::new));

	public ConditionHolder(ContextualCondition<T> condition) {
		this(condition, false, Optional.empty());
	}

	static <T extends ContextualCondition<T>> ConditionHolder<T> fromNetwork(FriendlyByteBuf buf) {
		return new ConditionHolder<>(
				(T) CommonProxy.readRegistryId(LycheeRegistries.CONTEXTUAL, buf).fromNetwork(buf),
				buf.readBoolean(),
				buf.readOptional(FriendlyByteBuf::readComponent)
		);
	}

	public void toNetwork(FriendlyByteBuf buf) {
		CommonProxy.writeRegistryId(LycheeRegistries.CONTEXTUAL, condition.type(), buf);
		buf.writeBoolean(secret);
		buf.writeOptional(description, FriendlyByteBuf::writeComponent);
	}
}
