package snownee.lychee.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.advancements.critereon.MinMaxBounds;

@Mixin(MinMaxBounds.Doubles.class)
public interface DoublesAccess {

	@Invoker(value = "<init>", remap = false)
	static MinMaxBounds.Doubles create(@Nullable Double min, @Nullable Double max) {
		throw new IllegalStateException();
	}

}
