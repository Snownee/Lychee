package snownee.lychee.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record Delay(PostActionCommonProperties commonProperties, float seconds) implements PostAction {

	public Delay(float seconds) {
		this(PostActionCommonProperties.EMPTY, seconds);
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

	public static class Type implements PostActionType<Delay> {
		public static final MapCodec<Delay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				PostActionCommonProperties.MAP_CODEC.forGetter(Delay::commonProperties),
				ExtraCodecs.POSITIVE_FLOAT.fieldOf("s").forGetter(Delay::seconds)
		).apply(instance, Delay::new));

		@Override
		public @NotNull MapCodec<Delay> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, Delay> streamCodec() {
			throw new UnsupportedOperationException();
		}
	}

}
