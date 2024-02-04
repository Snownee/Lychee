package snownee.lychee.util.contextual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ContextualImpl(ContextualCommonHolder contextualCommonHolder)
		implements Contextual<ContextualImpl>,
				   ContextualByCommonHolder<ContextualImpl> {

	public static final Codec<ContextualImpl> CODEC =
			RecordCodecBuilder.create(instance -> instance
					.group(ContextualCommonHolder.CODEC
							.fieldOf("conditions")
							.orElseGet(ContextualCommonHolder::new)
							.forGetter(ContextualImpl::contextualCommonHolder))
					.apply(instance, ContextualImpl::new));
}
