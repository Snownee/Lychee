package snownee.lychee.mixin.predicates;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;

@Mixin(LocationPredicate.PositionPredicate.class)
public interface PositionPredicateAccess {
	@Invoker("of")
	static Optional<LocationPredicate.PositionPredicate> of(
			MinMaxBounds.Doubles x,
			MinMaxBounds.Doubles y,
			MinMaxBounds.Doubles z
	) {
		throw new IllegalStateException();
	}
}
