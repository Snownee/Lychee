package snownee.lychee.core;

import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;

import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.LycheeRecipe;

public class ActionRuntime {

	public boolean doDefault;
	public State state;
	public final LinkedList<Job> jobs = Lists.newLinkedList();

	public enum State {
		RUNNING, PAUSED, STOPPED
	}

	public ActionRuntime() {
		reset();
	}

	public void reset() {
		doDefault = true;
		state = State.RUNNING;
		jobs.clear();
	}

	public void applySequence(List<PostAction> actions, @Nullable LycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		jobs.addAll(0, actions.stream().map($ -> new Job($, times)).toList());
		while (!jobs.isEmpty()) {
			jobs.pop().apply(recipe, ctx);
			if (ctx.runtime.state != State.RUNNING) {
				break;
			}
		}
	}

}
