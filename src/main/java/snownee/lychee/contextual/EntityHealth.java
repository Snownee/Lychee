package snownee.lychee.contextual;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.MinMaxBounds.Doubles;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;
import snownee.lychee.util.contextual.ContextualCondition;
import snownee.lychee.util.contextual.ContextualConditionType;
import snownee.lychee.util.recipe.ILycheeRecipe;

public record EntityHealth(Doubles range) implements ContextualCondition {

	@Override
	public ContextualConditionType<EntityHealth> type() {
		return ContextualConditionType.ENTITY_HEALTH;
	}

	@Override
	public int test(@Nullable ILycheeRecipe<?> recipe, LycheeContext ctx, int times) {
		Entity entity = ctx.get(LycheeContextKey.LOOT_PARAMS).get(LootContextParams.THIS_ENTITY);
		double health = 0;
		if (entity instanceof LivingEntity living) {
			health = living.getHealth();
		}
		return range.matches(health) ? times : 0;
	}

	@Override
	public MutableComponent getDescription(boolean inverted) {
		return Component.translatable(getDescriptionId(inverted), BoundsExtensions.getDescription(range));
	}

	public static class Type implements ContextualConditionType<EntityHealth> {
		public static final Codec<EntityHealth> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Doubles.CODEC.fieldOf("range").forGetter(EntityHealth::range)
		).apply(instance, EntityHealth::new));

		@Override
		public @NotNull Codec<EntityHealth> codec() {
			return CODEC;
		}
	}
}
