package snownee.lychee.context;

import java.util.Queue;
import java.util.stream.Stream;

import com.google.common.collect.Queues;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import snownee.lychee.Lychee;
import snownee.lychee.util.action.ClientSideStrategy;
import snownee.lychee.util.action.Job;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.codec.LycheeCodecs;
import snownee.lychee.util.context.LycheeContext;

public class ActionContext {

	public enum State {
		RUNNING, PAUSED, STOPPED
	}

	public static final Codec<ActionContext> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.BOOL.optionalFieldOf("avoid_default", false).forGetter(it -> it.avoidDefault),
					Codec.INT.fieldOf("state")
							.flatXmap(it -> LycheeCodecs.tryCatch(() -> State.values()[it]), it -> DataResult.success(it.ordinal()))
							.forGetter(it -> it.state),
					Codec.list(Job.CODEC).fieldOf("jobs").<Queue<Job>>xmap(
							Queues::newLinkedBlockingQueue,
							it -> it.stream().toList()
					).orElse(Queues.newLinkedBlockingQueue()).forGetter(it -> it.jobs)
			).apply(instance, ActionContext::new));

	public boolean avoidDefault = false;
	public State state = State.RUNNING;
	protected final Queue<Job> jobs;

	public ActionContext() {
		jobs = Queues.newLinkedBlockingQueue();
	}

	ActionContext(final boolean avoidDefault, final State state, final Queue<Job> jobs) {
		this.avoidDefault = avoidDefault;
		this.state = state;
		this.jobs = jobs;
	}

	public void reset() {
		avoidDefault = false;
		state = State.RUNNING;
		jobs.clear();
	}

	public void appendAction(LycheeContext context, PostAction action, int times) {
		if (context.level().isClientSide() && action.clientSideStrategy() != ClientSideStrategy.ALLOW_CLIENT_RUN) {
			return;
		}
		jobs.add(new Job(action, times));
	}

	public void appendActions(LycheeContext context, Stream<PostAction> actions, int times) {
		actions.forEach(action -> {
			if (context.level().isClientSide() && action.clientSideStrategy() != ClientSideStrategy.ALLOW_CLIENT_RUN) {
				return;
			}
			jobs.add(new Job(action, times));
		});
	}

	public void run(LycheeContext context) {
		while (!jobs.isEmpty()) {
			final var job = jobs.poll();
			if (context.level().isClientSide() && job.action().clientSideStrategy() != ClientSideStrategy.ALLOW_CLIENT_RUN) {
				continue;
			}
			try {
				job.apply(context);
				if (state != State.RUNNING) {
					break;
				}
			} catch (Throwable e) {
				Lychee.LOGGER.error("Error running action {}", job.action().type(), e);
				state = State.STOPPED;
			}
		}

		if (state == State.RUNNING || jobs.isEmpty()) {
			state = State.STOPPED;
		}
	}
}
