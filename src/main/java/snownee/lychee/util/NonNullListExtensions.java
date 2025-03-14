package snownee.lychee.util;

import java.util.Collection;
import java.util.function.Function;

import com.mojang.serialization.Codec;

import net.minecraft.core.NonNullList;

public class NonNullListExtensions {
	public static <E> Codec<NonNullList<E>> codecOf(Codec<E> entryCodec) {
		return entryCodec.listOf().xmap(NonNullList::copyOf, Function.identity());
	}

	public static <E> NonNullList<E> copyOf(Collection<? extends E> entries) {
		return NonNullList.copyOf(entries);
	}
}
