package snownee.lychee.util.contextual;

import java.util.List;

/**
 * Using {@link ContextualConditionsHolder} implement {@link Contextual} conditions. Reduce duplicate code.
 * @param <C>
 */
public interface ContextualByConditionsHolder<C extends ContextualByConditionsHolder<C>> extends Contextual<C> {
	ContextualConditionsHolder conditionsHolder();

	@Override
	default List<ConditionHolder<?>> conditions() {
		return conditionsHolder().conditions();
	}

	@Override
	default <T extends ContextualCondition<T>> void addCondition(final ConditionHolder<T> condition) {
		conditionsHolder().addCondition(condition);
	}
}
