package snownee.lychee.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.NonNullList;

@Mixin(NonNullList.class)
public interface NonNullListAccess {
	@Invoker("<init>")
	static <T> NonNullList<T> construct(List<T> list, T defaultValue) {
		throw new UnsupportedOperationException();
	}
}
