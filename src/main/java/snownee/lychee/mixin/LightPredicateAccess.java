package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.advancements.critereon.LightPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;

@Mixin(LightPredicate.class)
public interface LightPredicateAccess {

	@Accessor
	MinMaxBounds.Ints getComposite();

}
