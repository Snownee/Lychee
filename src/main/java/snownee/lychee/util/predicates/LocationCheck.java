package snownee.lychee.util.predicates;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import snownee.lychee.mixin.predicates.LocationCheckAccessor;

/**
 * LootItemCondition that checks the {@link LootContextParams.ORIGIN} position against a {@link LocationPredicate}
 * after applying an offset to the origin position.
 */
public record LocationCheck(Optional<LocationPredicate> predicate, BlockPos offset) implements LootItemCondition {
	public static final Codec<LocationCheck> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "predicate").forGetter(LocationCheck::predicate),
			LocationCheckAccessor.getOffsetCodec().forGetter(LocationCheck::offset)
	).apply(instance, LocationCheck::new));

	@Override
	public @NotNull LootItemConditionType getType() {
		return LootItemConditions.LOCATION_CHECK;
	}

	public boolean test(LootContext context) {
		final var vec3 = context.getParamOrNull(LootContextParams.ORIGIN);
		return vec3 != null && (this.predicate.isEmpty() || this.predicate.get().matches(
				context.getLevel(),
				vec3.x() + (double) this.offset.getX(),
				vec3.y() + (double) this.offset.getY(),
				vec3.z() + (double) this.offset.getZ()
		));
	}

	public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder builder) {
		return () -> new LocationCheck(Optional.of(builder.build()), BlockPos.ZERO);
	}

	public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder builder, BlockPos offset) {
		return () -> new LocationCheck(Optional.of(builder.build()), offset);
	}
}
