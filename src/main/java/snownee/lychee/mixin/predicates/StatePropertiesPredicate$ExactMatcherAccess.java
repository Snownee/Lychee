package snownee.lychee.mixin.predicates;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;

@Mixin(StatePropertiesPredicate.ExactMatcher.class)
public interface StatePropertiesPredicate$ExactMatcherAccess {
	@Invoker("<init>")
	static StatePropertiesPredicate.ExactMatcher create(String value) {
		throw new AssertionError();
	}
}
