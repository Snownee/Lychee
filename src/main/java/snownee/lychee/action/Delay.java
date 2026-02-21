package snownee.lychee.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.Lychee;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.action.ActionData;
import snownee.lychee.util.action.ActionMarker;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.action.PostActionTypes;
import snownee.lychee.util.context.LycheeContext;

public record Delay(PostActionCommonProperties commonProperties, float seconds) implements PostAction {

	public Delay(float seconds) {
		this(PostActionCommonProperties.EMPTY, seconds);
	}

	@Override
	public PostActionType<Delay> type() {
		return PostActionTypes.DELAY;
	}

	@Override
	public void apply(LycheeContext context, ActionContext actionContext, int times) {
		var actionMarker = actionContext.marker;
		if (actionMarker == null) {
			var marker = new Marker(EntityType.MARKER, context.level());
			var pos = actionContext.getOrNull(LootContextParams.ORIGIN);
			if (pos != null) {
				marker.setPos(pos);
			}
			marker.setCustomName(Component.literal(Lychee.ID));
			context.level().addFreshEntity(marker);
			actionContext.marker = actionMarker = (ActionMarker) marker;
			actionMarker.lychee$setData(new ActionData(context, actionContext, 0));
		}
		var actionData = actionMarker.lychee$getData();
		if (actionData == null) {
			Lychee.LOGGER.error("Delay action called without data: {}", context);
			return;
		}
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
		public MapCodec<Delay> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, Delay> streamCodec() {
			throw new UnsupportedOperationException();
		}
	}

}
