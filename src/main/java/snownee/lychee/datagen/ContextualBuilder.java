package snownee.lychee.datagen;

import java.util.List;

import org.jetbrains.annotations.Contract;

import com.google.common.collect.Lists;

import dev.latvian.mods.rhino.util.RemapForJS;
import snownee.lychee.contextual.Chance;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionData;
import snownee.lychee.util.contextual.ContextualHolder;

public class ContextualBuilder<T> {
	protected final List<ContextualConditionData<?>> conditions = Lists.newArrayListWithExpectedSize(4);

	@Contract("-> this")
	protected final T self() {
		//noinspection unchecked
		return (T) this;
	}

	public ContextualHolder contextualHolder() {
		return ContextualHolder.pack(conditions);
	}

	@Contract("_ -> this")
	public T condition(ContextualCondition condition) {
		return condition(new ContextualConditionData<>(condition));
	}

	@Contract("_ -> this")
	@RemapForJS("conditionData")
	public T condition(ContextualConditionData<?> condition) {
		conditions.add(condition);
		return self();
	}

	@Contract("_ -> this")
	@RemapForJS("conditionHolder")
	public T condition(ContextualHolder conditions) {
		this.conditions.addAll(conditions.unpack());
		return self();
	}

	@Contract("_ -> this")
	public T chance(float chance) {
		return condition(new Chance(chance));
	}
}
