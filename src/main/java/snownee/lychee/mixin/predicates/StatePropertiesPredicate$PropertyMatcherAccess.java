package snownee.lychee.mixin.predicates;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;

@Mixin(StatePropertiesPredicate.PropertyMatcher.class)
public interface StatePropertiesPredicate$PropertyMatcherAccess {
	@Invoker("<init>")
	static StatePropertiesPredicate.PropertyMatcher create(String name, StatePropertiesPredicate.ValueMatcher valueMatcher) {
		throw new AssertionError();
	}
}
