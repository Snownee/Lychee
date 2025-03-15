package snownee.lychee.action;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.codec.LycheeStreamCodecs;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record Exit(PostActionCommonProperties commonProperties) implements PostAction {

	public static final Exit CLIENT_DUMMY = new Exit();

	public Exit() {
		this(new PostActionCommonProperties());
	}

	@Override
	public PostActionType<Exit> type() {
		return PostActionTypes.EXIT;
	}

	@Override
	public void apply(@Nullable ILycheeRecipe<?> recipe, LycheeContext context, int times) {
		context.get(LycheeContextKey.ACTION).state = ActionContext.State.STOPPED;
	}

	@Override
	public boolean hidden() {
		return true;
	}

	public static class Type implements PostActionType<Exit> {
		public static final MapCodec<Exit> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(Exit::commonProperties)
		).apply(instance, Exit::new));

		@Override
		public MapCodec<Exit> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, Exit> streamCodec() {
			return LycheeStreamCodecs.uncheckedUnit(CLIENT_DUMMY);
		}
	}
}
