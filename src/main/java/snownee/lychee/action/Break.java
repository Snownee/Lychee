package snownee.lychee.action;

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

public record Break(PostActionCommonProperties commonProperties) implements PostAction {

	public static final Break CLIENT_DUMMY = new Break();

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
