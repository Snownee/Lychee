package snownee.lychee.util.action;


import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import snownee.lychee.util.context.LycheeContext;

public final class ActionData {
	public static final Codec<ActionData> CODEC = RecordCodecBuilder.create(i -> i.group(
			LycheeContext.CODEC.fieldOf("context").forGetter(ActionData::getContext),
			Codec.INT.fieldOf("delayedTicks").forGetter(ActionData::getDelayedTicks)
	).apply(i, ActionData::new));

	private LycheeContext context;
	private int delayedTicks;

	public ActionData(LycheeContext context, int delayedTicks) {
		this.context = context;
		this.delayedTicks = delayedTicks;
	}

	public LycheeContext getContext() {
		return context;
	}

	public void setContext(final @Nullable LycheeContext context) {
		this.context = context;
	}

	public int getDelayedTicks() {
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
		return new ToStringBuilder(this)
				.append("context", context.toString())
				.append("delayedTicks", delayedTicks)
				.toString();
	}
}
