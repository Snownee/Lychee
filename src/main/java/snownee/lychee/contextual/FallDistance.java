package snownee.lychee.contextual;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.MinMaxBounds.Doubles;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record FallDistance(Doubles range) implements ContextualCondition<FallDistance> {

	@Override
	public ContextualConditionType<FallDistance> type() {
		return ContextualConditionType.FALL_DISTANCE;
	}

	@Override
	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		final var entity = ctx.get(LycheeContextKey.LOOT_PARAMS).get(LootContextParams.THIS_ENTITY);
		var distance = entity.fallDistance;
		if (entity instanceof FallingBlockEntity block) {
			distance = Math.max(block.getStartPos().getY() - block.getBlockY(), distance);
		}
		return range.matches(distance) ? times : 0;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		return Component.translatable(getDescriptionId(inverted), BoundsExtensions.getDescription(range));
	}

	public static class Type implements ContextualConditionType<FallDistance> {
		public static final Codec<FallDistance> CODEC = RecordCodecBuilder.create(instance -> instance
				.group(Doubles.CODEC.fieldOf("range").forGetter(FallDistance::range))
				.apply(instance, FallDistance::new));

		@Override
		public Codec<FallDistance> codec() {
			return CODEC;
		}
	}
}
