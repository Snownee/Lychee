package snownee.lychee.util.contextual;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ContextualImpl(List<ConditionHolder<?>> conditions) implements Contextual<ContextualImpl> {

	public static final Codec<ContextualImpl> CODEC =
			RecordCodecBuilder.create(instance -> instance.group(Codec.list(ConditionHolder.CODEC)
																	  .fieldOf("conditions")
																	  .orElse(Lists.newArrayList())
																	  .forGetter(Contextual::conditions))
														  .apply(instance, ContextualImpl::new));

	@Override
	public List<ConditionHolder<?>> conditions() {
		return ImmutableList.copyOf(conditions);
	}

	@Override
	public <T extends ContextualCondition<T>> void addCondition(ConditionHolder<T> condition) {
		conditions.add(condition);
	}

	@Override
	public Codec<ContextualImpl> codec() {
		return CODEC;
	}
}
