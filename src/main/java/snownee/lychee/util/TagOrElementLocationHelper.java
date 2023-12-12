package snownee.lychee.util;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

public final class TagOrElementLocationHelper {
	public static  <T> HolderSet<T> get(ExtraCodecs.TagOrElementLocation location, Registry<T> registry) {
		if (location.tag()) return registry.getOrCreateTag(TagKey.create(registry.key(), location.id()));
		return HolderSet.direct(Holder.direct(registry.get(location.id())));
	}
}
