package snownee.lychee.util.contextual;

import java.util.List;

/**
 * Using {@link ContextualCommonHolder} implement {@link Contextual} conditions. Reduce duplicate code.
 *
 * @param <C>
 */
public interface ContextualByCommonHolder<C extends ContextualByCommonHolder<C>> extends Contextual<C> {
	ContextualCommonHolder contextualCommonHolder();

	@Override
	default List<ConditionHolder<?>> conditions() {
		return contextualCommonHolder().conditions();
	}

	@Override
	default <T extends ContextualCondition<T>> void addCondition(final ConditionHolder<T> condition) {
		contextualCommonHolder().addCondition(condition);
	}
}
