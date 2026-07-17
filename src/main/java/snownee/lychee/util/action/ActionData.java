package snownee.lychee.util.action;


import java.util.Queue;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.collect.Queues;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.level.Level;
import snownee.kiwi.util.codec.KCodecs;
import snownee.lychee.LycheeLootContextParamSets;
import snownee.lychee.context.ActionContext;
import snownee.lychee.context.LootParamsContext;
import snownee.lychee.util.context.LycheeContext;

public final class ActionData {
	private LycheeContext context;
	private ActionContext actionContext;
	private int delayedTicks;

	public ActionData(LycheeContext context, ActionContext actionContext, int delayedTicks) {
		this.context = context;
		this.actionContext = actionContext;
		this.delayedTicks = delayedTicks;
	}

	public LycheeContext context() {
		return context;
	}

	public ActionContext actionContext() {
		return actionContext;
	}

	public int delayedTicks() {
		return delayedTicks;
	}

	public int consumeDelayedTicks() {
		return delayedTicks--;
	}

	public void setDelayedTicks(final int delayedTicks) {
		this.delayedTicks = delayedTicks;
	}

	public void addDelayedTicks(int ticks) {
		this.delayedTicks += ticks;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("context", context.toString()).append("delayedTicks", delayedTicks).toString();
	}

	public record Builder(LycheeContext context, int delayedTicks, boolean avoidDefault, ActionContext.State state, Queue<Job> jobs) {
		public static final Codec<Builder> CODEC = RecordCodecBuilder.create(i -> i.group(
				LycheeContext.CODEC.fieldOf("context").forGetter(Builder::context),
				Codec.INT.fieldOf("delayedTicks").forGetter(Builder::delayedTicks),
				Codec.BOOL.optionalFieldOf("avoidDefault", false).forGetter(Builder::avoidDefault),
				Codec.INT.fieldOf("state")
						.flatXmap(it -> KCodecs.tryCatch(() -> ActionContext.State.values()[it]), it -> DataResult.success(it.ordinal()))
						.forGetter(Builder::state),
				Codec.list(Job.CODEC)
						.fieldOf("jobs")
						.<Queue<Job>>xmap(Queues::newLinkedBlockingQueue, it -> it.stream().toList())
						.orElse(Queues.newLinkedBlockingQueue())
						.forGetter(Builder::jobs)).apply(i, ActionData.Builder::new));

		public ActionData build(Level level) {
			LootParamsContext lootParams = new LootParamsContext(level, LycheeLootContextParamSets.ALL);
			ActionContext actionContext = new ActionContext(avoidDefault, state, jobs, lootParams);
			return new ActionData(context, actionContext, delayedTicks);
		}
	}
}
