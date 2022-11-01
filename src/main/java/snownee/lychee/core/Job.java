package snownee.lychee.core;

import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.LycheeRecipe;

public class Job {
	public PostAction action;
	public int times;

	public Job(PostAction action, int times) {
		this.action = action;
		this.times = times;
	}

	public void apply(LycheeRecipe<?> recipe, LycheeContext ctx) {
		int t = action.checkConditions(recipe, ctx, times);
		if (t > 0) {
			action.doApply(recipe, ctx, t);
		}
	}
}
