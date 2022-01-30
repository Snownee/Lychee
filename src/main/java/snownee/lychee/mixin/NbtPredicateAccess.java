package snownee.lychee.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.CompoundTag;

@Mixin(NbtPredicate.class)
public interface NbtPredicateAccess {

	@Accessor
	@Nullable
	CompoundTag getTag();

}
