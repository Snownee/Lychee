package snownee.lychee.util.predicates;

import java.util.Optional;

import net.minecraft.advancements.criterion.StatePropertiesPredicate;

public class PropertiesPredicateExtensions {

	public static Optional<StatePropertiesPredicate.PropertyMatcher> findMatcher(
			StatePropertiesPredicate predicate,
			String name
	) {
		return predicate.properties()
				.stream()
				.filter($ -> $.name().equals(name))
				.findAny();
	}
}
