package snownee.lychee.util.predicates;

import java.util.Optional;

import net.minecraft.advancements.criterion.EntityPredicate;

public class EntityPredicateExtensions {
	public static EntityPredicate withoutType(EntityPredicate original) {
		return new EntityPredicate(
				Optional.empty(),
				original.distanceToPlayer(),
				original.movement(),
				original.location(),
				original.effects(),
				original.nbt(),
				original.flags(),
				original.equipment(),
				original.subPredicate(),
				original.periodicTick(),
				original.vehicle(),
				original.passenger(),
				original.targetedEntity(),
				original.team(),
				original.slots(),
				original.components());
	}
}
