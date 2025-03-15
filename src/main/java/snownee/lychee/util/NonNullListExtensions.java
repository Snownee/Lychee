package snownee.lychee.util;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.mojang.serialization.Codec;

import net.minecraft.core.NonNullList;
import snownee.lychee.mixin.NonNullListAccess;

public class NonNullListExtensions {
	public static <E> Codec<NonNullList<E>> codecOf(Codec<E> entryCodec) {
		return entryCodec.listOf().xmap(NonNullListExtensions::copyOf, Function.identity());
	}

	public static <E> NonNullList<E> copyOf(Collection<? extends E> entries) {
		return NonNullListAccess.construct(List.copyOf(entries), null);
	}
}
