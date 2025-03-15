package snownee.lychee.util.contextual;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ContextualConditionData(ContextualCondition condition, boolean secret, Optional<Component> description) {
	public static final MapCodec<ContextualConditionData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ContextualCondition.CODEC.forGetter(ContextualConditionData::condition),
			Codec.BOOL.optionalFieldOf("secret", false).forGetter(ContextualConditionData::secret),
			ComponentSerialization.CODEC.optionalFieldOf("description").forGetter(ContextualConditionData::description)
	).apply(instance, ContextualConditionData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextualConditionData> STREAM_CODEC = StreamCodec.composite(
			ContextualCondition.STREAM_CODEC,
			ContextualConditionData::condition,
			ByteBufCodecs.BOOL,
			ContextualConditionData::secret,
			ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC),
			ContextualConditionData::description,
			ContextualConditionData::new);

	public ContextualConditionData(ContextualCondition condition) {
		this(condition, false, Optional.empty());
	}
}
