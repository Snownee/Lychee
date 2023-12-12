package snownee.lychee.contextual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.MinMaxBounds.Doubles;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.contextual.ContextualConditionTypes;

public record FallDistance(Doubles range) implements ContextualCondition<FallDistance> {

	@Override
	public ContextualConditionType<FallDistance> type() {
		return ContextualConditionTypes.FALL_DISTANCE;
	}

	@Override
	public int test(RecipeHolder<LycheeRecipe<?>> recipe, LycheeRecipeContext ctx, int times) {
		final var entity = ctx.get(LootContextParams.THIS_ENTITY);
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
