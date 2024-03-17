package snownee.lychee.util.action;


import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.ExtraCodecs;
import snownee.lychee.util.context.LycheeContext;

public final class ActionData {
	public static final Codec<ActionData> CODEC =
			RecordCodecBuilder.create(inst ->
					inst.group(
							ExtraCodecs.strictOptionalField(LycheeContext.CODEC, "context").forGetter(ActionData::getContext),
							Codec.INT.fieldOf("delayedTicks").forGetter(ActionData::getDelayedTicks)
					).apply(inst, ActionData::of));
	@Nullable
	private LycheeContext context;
	private int delayedTicks;

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private static ActionData of(Optional<LycheeContext> context, int delayedTicks) {
		return new ActionData(context.orElse(null), delayedTicks);
	}

	private ActionData(
			@Nullable LycheeContext context,
			int delayedTicks
	) {
		this.context = context;
		this.delayedTicks = delayedTicks;
	}

	public static ActionData of(@Nullable LycheeContext context, int delayedTicks) {
		if (context == null) {
			return new ActionData(null, delayedTicks);
		}
		return new ActionData(context, delayedTicks);
	}

	public Optional<LycheeContext> getContext() {
		return Optional.ofNullable(context);
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
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final ActionData that = (ActionData) o;
		return delayedTicks == that.delayedTicks && Objects.equal(context, that.context);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(context, delayedTicks);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("context", context)
				.add("delayedTicks", delayedTicks)
				.toString();
	}
}
