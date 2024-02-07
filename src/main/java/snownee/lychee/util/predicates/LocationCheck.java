package snownee.lychee.util.predicates;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import snownee.lychee.mixin.predicates.LocationCheckAccess;

/**
 * LootItemCondition that checks the {@link LootContextParams.ORIGIN} position against a {@link LocationPredicate}
 * after applying an offset to the origin position.
 */
public record LocationCheck(LocationPredicate predicate, BlockPos offset) implements LootItemCondition {
	public static final Codec<LocationCheck> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			LocationPredicate.CODEC.fieldOf("predicate").forGetter(LocationCheck::predicate),
			LocationCheckAccess.getOffsetCodec().forGetter(LocationCheck::offset)
	).apply(instance, LocationCheck::new));

	@Override
	public @NotNull LootItemConditionType getType() {
		return LootItemConditions.LOCATION_CHECK; //FIXME
	}

	public boolean test(LootContext context) {
		final var vec3 = context.getParamOrNull(LootContextParams.ORIGIN);
		return vec3 != null && this.predicate.matches(
				context.getLevel(),
				vec3.x() + this.offset.getX(),
				vec3.y() + this.offset.getY(),
				vec3.z() + this.offset.getZ()
		);
	}

	public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder builder) {
		return () -> new LocationCheck(builder.build(), BlockPos.ZERO);
	}

	public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder builder, BlockPos offset) {
		return () -> new LocationCheck(builder.build(), offset);
	}
}
