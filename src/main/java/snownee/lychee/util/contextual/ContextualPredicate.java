package snownee.lychee.util.contextual;

import snownee.lychee.context.ActionContext;
import snownee.lychee.util.context.LycheeContext;

public interface ContextualPredicate {
	/**
	 * @param ctx           Context
	 * @param actionContext
	 * @param times         Time of request to execute
	 * @return Executable time after condition
	 */
	int test(LycheeContext ctx, ActionContext actionContext, int times);
}
