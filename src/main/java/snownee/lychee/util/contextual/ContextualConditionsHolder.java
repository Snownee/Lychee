package snownee.lychee.util.contextual;

import java.util.List;

import org.apache.commons.compress.utils.Lists;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;

/**
 * For the classes that implemented {@link Contextual} to delegate for reducing duplicate code. <br>
 * Mainly for records since they can't extend abstract
 *
 * @param conditions
 */
public record ContextualConditionsHolder(List<ConditionHolder<?>> conditions) {
	public static final Codec<ContextualConditionsHolder> CODEC =
			ConditionHolder.LIST_CODEC.xmap(ContextualConditionsHolder::new, it -> it.conditions);

	public ContextualConditionsHolder() {
		this(Lists.newArrayList());
	}

	@Override
	public List<ConditionHolder<?>> conditions() {
		return ImmutableList.copyOf(conditions);
	}

	public <T extends ContextualCondition<T>> void addCondition(ConditionHolder<T> condition) {
		conditions.add(condition);
	}
}
