package snownee.lychee.context;

import java.util.Queue;

import com.google.common.collect.Queues;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.Lychee;
import snownee.lychee.util.SerializableType;
import snownee.lychee.util.action.Job;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextType;
import snownee.lychee.util.context.LycheeContextTypes;
import snownee.lychee.util.context.LycheeContextValue;
import snownee.lychee.util.recipe.LycheeRecipe;

public class ActionContext implements LycheeContextValue<ActionContext> {
	public boolean avoidDefault = false;
	public State state = State.RUNNING;
	public Queue<Job> jobs = Queues.newLinkedBlockingQueue();

	public ActionContext() {
	}

	ActionContext(final boolean avoidDefault, final State state, final Queue<Job> jobs) {
		this.avoidDefault = avoidDefault;
		this.state = state;
		this.jobs = jobs;
	}

	@Override
	public LycheeContextType<ActionContext> type() {
		return LycheeContextTypes.ACTION;
	}

	public enum State {
		RUNNING, PAUSED, STOPPED
	}

	public void run(RecipeHolder<LycheeRecipe<?>> recipe, LycheeContext context) {
		while (!jobs.isEmpty()) {
			final var job = jobs.poll();
			try {
				job.apply(recipe, context);
				if (state != State.RUNNING) {
					break;
				}
			} catch (Throwable e) {
				// TODO 需要检查这里提供的类型是否可读
				Lychee.LOGGER.error("Error running action {}", job.action().type(), e);
				state = State.STOPPED;
			}
		}

		if (state == State.RUNNING || jobs.isEmpty()) {
			state = State.STOPPED;
		}
	}


	public static final class Type implements LycheeContextType<ActionContext>,
											  SerializableType<ActionContext> {
		public static final Codec<ActionContext> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						Codec.BOOL.fieldOf("avoid_default").orElse(false).forGetter(it -> it.avoidDefault),
						Codec.INT.fieldOf("state")
								 .flatXmap(it -> {
									 try {
										 return DataResult.success(State.values()[it]);
									 } catch (Throwable t) {
										 return DataResult.error(t::getMessage);
									 }
								 }, it -> DataResult.success(it.ordinal()))
								 .forGetter(it -> it.state),
						Codec.list(Job.CODEC).fieldOf("jobs").<Queue<Job>>xmap(
								Queues::newLinkedBlockingQueue,
								it -> it.stream().toList()
						).orElse(Queues.newLinkedBlockingQueue()).forGetter(it -> it.jobs)
				).apply(instance, ActionContext::new));

		@Override
		public Codec<ActionContext> codec() {
			return CODEC;
		}
	}
}
