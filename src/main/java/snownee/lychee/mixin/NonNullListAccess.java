package snownee.lychee.mixin;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.NonNullList;

@Mixin(NonNullList.class)
public interface NonNullListAccess {
	@Invoker("<init>")
	static <E> NonNullList<E> construct(List<E> list, @Nullable E defaultValue) {
		throw new AssertionError();
	}
}
