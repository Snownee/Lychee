package snownee.lychee.context;

import java.util.Queue;

import com.google.common.collect.Queues;

import com.mojang.serialization.Codec;

import snownee.lychee.util.action.Job;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextType;
import snownee.lychee.util.context.LycheeContextValue;
import snownee.lychee.util.context.SerializableContextType;

public class ActionContext implements LycheeContextValue<ActionContext> {
	public boolean avoidDefault = false;
	public State state = State.RUNNING;
	public final Queue<Job> jobs = Queues.newLinkedBlockingQueue();

	@Override
	public LycheeContextType<ActionContext> type() {
		return null;
	}

	public enum State {
		RUNNING, PAUSED, STOPPED
	}

	public static final class Type implements LycheeContextType<ActionContext>,
                                              SerializableContextType<ActionContext> {


		@Override
		public Codec<ActionContext> codec() {
			return null;
		}

		@Override
		public ActionContext construct(final LycheeContext context) {
			return new ActionContext();
		}
	}
}
