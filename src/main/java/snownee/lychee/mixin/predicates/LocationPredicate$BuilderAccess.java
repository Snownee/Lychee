package snownee.lychee.mixin.predicates;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;

@Mixin(LocationPredicate.Builder.class)
public interface LocationPredicate$BuilderAccess {
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	@Accessor
	void setBlock(Optional<BlockPredicate> blockPredicate);
}
