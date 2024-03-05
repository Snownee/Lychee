package snownee.lychee.action;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

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

public final class Break implements PostAction {

	public static final Break CLIENT_DUMMY = new Break();
	private final PostActionCommonProperties commonProperties;

	public Break(PostActionCommonProperties commonProperties) {this.commonProperties = commonProperties;}

	public Break() {
		this(new PostActionCommonProperties());
	}

	@Override
	public PostActionType<Break> type() {
		return PostActionTypes.BREAK;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		context.get(LycheeContextKey.ACTION).state = ActionContext.State.STOPPED;
	}

	@Override
	public boolean hidden() {
		return true;
	}

	@Override
	public PostActionCommonProperties commonProperties() {return commonProperties;}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		var that = (Break) obj;
		return Objects.equals(this.commonProperties, that.commonProperties);
	}

	@Override
	public int hashCode() {
		return Objects.hash(commonProperties);
	}

	@Override
	public String toString() {
		return "Break[" +
				"commonProperties=" + commonProperties + ']';
	}


	public static class Type implements PostActionType<Break> {
		public static final Codec<Break> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(Break::commonProperties)
		).apply(instance, Break::new));

		@Override
		public Codec<Break> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<? extends ByteBuf, Break> streamCodec() {
			return StreamCodec.of((buf, value) -> {}, (buf) -> CLIENT_DUMMY);
		}
	}
}
