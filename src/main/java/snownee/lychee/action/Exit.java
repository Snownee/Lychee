package snownee.lychee.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;

public record Exit(PostActionCommonProperties commonProperties) implements PostAction {
	public Exit() {
		this(PostActionCommonProperties.EMPTY);
	}

	@Override
	public PostActionType<Exit> type() {
		return PostActionTypes.EXIT;
	}

	@Override
	public void apply(LycheeContext context, ActionContext actionContext, int times) {
		actionContext.state = ActionContext.State.STOPPED;
	}

	@Override
	public boolean hidden() {
		return true;
	}

	public static class Type implements PostActionType<Exit> {
		public static final MapCodec<Exit> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(Exit::commonProperties)
		).apply(instance, Exit::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, Exit> STREAM_CODEC = PostActionCommonProperties.STREAM_CODEC.map(
				Exit::new,
				Exit::commonProperties);

		@Override
		public MapCodec<Exit> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, Exit> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
