package snownee.lychee.util.contextual;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;

/**
 * Record that implemented {@link Contextual}. <br>
 * For the classes that implemented {@link Contextual} to delegate for reducing duplicate code. <br>
 * Mainly for records since they can't extend abstract
 *
 * @param conditions
 */
public record ContextualConditionsHolder(List<ConditionHolder<?>> conditions)
		implements Contextual<ContextualConditionsHolder> {
	public static final Codec<ContextualConditionsHolder> CODEC =
			ConditionHolder.LIST_CODEC.xmap(ContextualConditionsHolder::new, it -> it.conditions);

	public ContextualConditionsHolder() {
		this(Lists.newArrayList());
	}

	@Override
	public List<ConditionHolder<?>> conditions() {
		return ImmutableList.copyOf(conditions);
	}

	@Override
	public <T extends ContextualCondition<T>> void addCondition(ConditionHolder<T> condition) {
		conditions.add(condition);
	}

	@Override
	public Codec<ContextualConditionsHolder> codec() {
		return CODEC;
	}
}
