package snownee.lychee.contextual;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.criterion.MinMaxBounds.Doubles;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.context.ActionContext;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;

public record FallDistance(Doubles range) implements ContextualCondition {

	@Override
	public ContextualConditionType<FallDistance> type() {
		return ContextualConditionType.FALL_DISTANCE;
	}

	@Override
	public int test(LycheeContext ctx, ActionContext actionContext, int times) {
		final var entity = actionContext.get(LootContextParams.THIS_ENTITY);
		var distance = entity.fallDistance;
		if (entity instanceof FallingBlockEntity block) {
			distance = (float) Math.max(block.getStartPos().getY() - block.getY(), distance);
		}
		return range.matches(distance) ? times : 0;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		return Component.translatable(getDescriptionId(inverted), BoundsExtensions.getDescription(range));
	}

	public static class Type implements ContextualConditionType<FallDistance> {
		public static final MapCodec<FallDistance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				Doubles.CODEC.fieldOf("range").forGetter(FallDistance::range)
		).apply(instance, FallDistance::new));

		@Override
		public MapCodec<FallDistance> codec() {
			return CODEC;
		}
	}
}
