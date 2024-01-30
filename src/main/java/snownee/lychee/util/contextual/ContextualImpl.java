package snownee.lychee.util.contextual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ContextualImpl(ContextualConditionsHolder conditionsHolder)
		implements Contextual<ContextualImpl>,
				   ContextualByConditionsHolder<ContextualImpl> {

	public static final Codec<ContextualImpl> CODEC =
			RecordCodecBuilder.create(instance -> instance
					.group(ContextualConditionsHolder.CODEC
								   .fieldOf("conditions")
								   .orElse(new ContextualConditionsHolder())
								   .forGetter(ContextualImpl::conditionsHolder))
					.apply(instance, ContextualImpl::new));

	@Override
	public Codec<ContextualImpl> contextualCodec() {
		return CODEC;
	}
}
