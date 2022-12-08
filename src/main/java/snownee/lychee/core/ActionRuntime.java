package snownee.lychee.core;

import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;

import snownee.lychee.Lychee;
import snownee.lychee.core.post.Delay.LycheeMarker;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.ILycheeRecipe;

public class ActionRuntime {

	public boolean doDefault = true;
	public State state = State.RUNNING;
	public final LinkedList<Job> jobs = Lists.newLinkedList();
	public LycheeMarker marker;

	public enum State {
		RUNNING, PAUSED, STOPPED
	}

	public void enqueue(List<PostAction> actions, int times) {
		jobs.addAll(0, actions.stream().map($ -> new Job($, times)).toList());
	}

	public void run(ILycheeRecipe<?> recipe, LycheeContext ctx) {
		try {
			while (!jobs.isEmpty()) {
				jobs.pop().apply(recipe, ctx);
				if (ctx.runtime.state != State.RUNNING) {
					break;
				}
			}
			if (ctx.runtime.state == State.RUNNING && jobs.isEmpty()) {
				ctx.runtime.state = State.STOPPED;
			}
		} catch (Throwable e) {
			Lychee.LOGGER.error("Error running actions");
			Lychee.LOGGER.catching(e);
			ctx.runtime.state = State.STOPPED;
		}
	}

}
