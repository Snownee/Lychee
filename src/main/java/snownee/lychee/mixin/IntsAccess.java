package snownee.lychee.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.advancements.critereon.MinMaxBounds;

@Mixin(MinMaxBounds.Ints.class)
public interface IntsAccess {

	@Invoker(value = "<init>", remap = false)
	static MinMaxBounds.Ints create(@Nullable Integer min, @Nullable Integer max) {
		throw new IllegalStateException();
	}

}
