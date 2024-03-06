package snownee.lychee.action;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public final class Delay implements PostAction {
	private final PostActionCommonProperties commonProperties;
	private final float seconds;

	public Delay(PostActionCommonProperties commonProperties, float seconds) {
		this.commonProperties = commonProperties;
		this.seconds = seconds;
	}

	public Delay(float seconds) {
		this.seconds = seconds;
		this.commonProperties = new PostActionCommonProperties();
	}

	@Override
	public PostActionType<Delay> type() {
		return PostActionTypes.DELAY;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		var actionContext = context.get(LycheeContextKey.ACTION);
		var actionMarker = context.get(LycheeContextKey.MARKER);
		var actionData = actionMarker.lychee$getData();
		context.put(LycheeContextKey.RECIPE, recipe);
		actionData.addDelayedTicks((int) (seconds * 20));
		actionContext.state = ActionContext.State.PAUSED;
	}

	@Override
	public boolean preventSync() {
		return true;
	}

	@Override
	public PostActionCommonProperties commonProperties() {return commonProperties;}

	public float seconds() {return seconds;}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		var that = (Delay) obj;
		return Objects.equals(this.commonProperties, that.commonProperties) &&
				Float.floatToIntBits(this.seconds) == Float.floatToIntBits(that.seconds);
	}

	@Override
	public int hashCode() {
		return Objects.hash(commonProperties, seconds);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("commonProperties", commonProperties)
				.add("seconds", seconds)
				.toString();
	}

	public static class Type implements PostActionType<Delay> {
		public static final Codec<Delay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(Delay::commonProperties),
				Codec.FLOAT.fieldOf("s").forGetter(Delay::seconds)
		).apply(instance, Delay::new));

		@Override
		public Codec<Delay> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<? extends ByteBuf, Delay> streamCodec() {
			throw new UnsupportedOperationException();
		}
	}

}
