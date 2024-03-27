package snownee.lychee.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;

public final class RegistryEntryDisplay {
	public static <T> MutableComponent of(ResourceKey<T> value, ResourceKey<Registry<T>> registry) {
		return Component.translatableWithFallback(
				value.location().toLanguageKey(registry.location().toShortLanguageKey()),
				CommonProxy.capitaliseAllWords(value.location().getPath())
		);
	}

	public static <T> MutableComponent of(Holder<T> holder, ResourceKey<Registry<T>> registry) {
		if (holder instanceof Holder.Reference<T> reference) {
			return Component.translatableWithFallback(
					reference.key().location().toLanguageKey(registry.location().toShortLanguageKey()),
					CommonProxy.capitaliseAllWords(reference.key().location().getPath())
			);
		}
		// There isn't key of Holder.Direct. Display the instance id.
		return Component.literal(holder.value() + "(" + holder.getRegisteredName() + ")");
	}


	public static <T> MutableComponent of(HolderSet<T> value, ResourceKey<Registry<T>> registry) {
		if (value instanceof HolderSet.Named<T> named) {
			return Component.translatableWithFallback(
					registry.location().toShortLanguageKey() + ".#" + named.key().location().toLanguageKey(),
					CommonProxy.capitaliseAllWords(named.key().location().getPath().replace('_', ' '))
			);
		}
		if (value.size() == 1) {
			return of(value.get(0), registry);
		}
		// TODO Need to consider how to display list backend HolderSet
		return value.stream().limit(value.size() - 1).reduce(
				Component.empty(),
				(component, holder) -> of(holder, registry),
				(prev, curr) -> prev.append(curr).append(", ")).append(of(value.get(value.size() - 1), registry));
	}
}
