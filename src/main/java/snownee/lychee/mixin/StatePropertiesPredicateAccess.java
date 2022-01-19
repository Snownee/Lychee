package snownee.lychee.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;

@Mixin(StatePropertiesPredicate.class)
public interface StatePropertiesPredicateAccess {

	@Accessor
	List<StatePropertiesPredicate.PropertyMatcher> getProperties();

}
