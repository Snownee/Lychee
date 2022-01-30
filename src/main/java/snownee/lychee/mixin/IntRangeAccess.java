package snownee.lychee.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

@Mixin(IntRange.class)
public interface IntRangeAccess {

	@Invoker(value = "<init>", remap = false)
	static IntRange create(@Nullable NumberProvider pMin, @Nullable NumberProvider pMax) {
		throw new IllegalStateException();
	}

	@Accessor
	@Nullable
	NumberProvider getMin();

	@Accessor
	@Nullable
	NumberProvider getMax();

}
