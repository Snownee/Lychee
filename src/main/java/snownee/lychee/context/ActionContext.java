package snownee.lychee.context;

import java.util.Map;
import java.util.Queue;

import org.jspecify.annotations.Nullable;

import com.google.common.collect.Queues;

import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootContext;
import snownee.lychee.Lychee;
import snownee.lychee.util.action.ActionMarker;
import snownee.lychee.util.action.Job;
import snownee.lychee.util.context.LycheeContext;

public class ActionContext implements LootParamsAccess {
	public boolean avoidDefault = false;
	public State state = State.RUNNING;
	public Queue<Job> jobs = Queues.newLinkedBlockingQueue();
	public @Nullable ActionMarker marker;
	final LootParamsContext lootParams;

	public ActionContext(LootParamsContext lootParams) {
		this.lootParams = lootParams;
	}

	public ActionContext(boolean avoidDefault, State state, Queue<Job> jobs, LootParamsContext lootParams) {
		this.avoidDefault = avoidDefault;
		this.state = state;
		this.jobs = jobs;
		this.lootParams = lootParams;
	}

	public void run(LycheeContext context) {
		while (!jobs.isEmpty()) {
			final var job = jobs.poll();
			try {
				job.apply(context, this);
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

	@Override
	public Map<ContextKey<?>, @Nullable Object> params() {
		return lootParams.params();
	}

	@Override
	public ContextKeySet paramSet() {
		return lootParams.paramSet();
	}

	@Override
	public boolean has(ContextKey<?> param) {
		return lootParams.has(param);
	}

	@Override
	public <T> T get(ContextKey<T> param) {
		return lootParams.get(param);
	}

	@Override
	public @Nullable <T> T getOrNull(ContextKey<T> param) {
		return lootParams.getOrNull(param);
	}

	@Override
	public <T> void set(ContextKey<T> param, @Nullable T value) {
		lootParams.set(param, value);
	}

	@Override
	public void remove(ContextKey<?> param) {
		lootParams.remove(param);
	}

	@Override
	public LootContext asLootContext() {
		return lootParams.asLootContext();
	}

	@Override
	public void initAll() {
		lootParams.initAll();
	}

	public enum State {
		RUNNING, PAUSED, STOPPED
	}
}
