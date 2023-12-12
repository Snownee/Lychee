package snownee.lychee.util;

import com.mojang.serialization.Codec;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;

public record TagOrElementHolder<T>(ResourceLocation id, boolean tag) {
	public static final Codec<TagOrElementHolder<?>> CODEC = ExtraCodecs.TAG_OR_ELEMENT_ID.xmap(
			it -> new TagOrElementHolder<>(it.id(), it.tag()),
			it -> new ExtraCodecs.TagOrElementLocation(it.id, it.tag)
	);

	public static <T> Codec<TagOrElementHolder<T>> codec() {
		return (Codec<TagOrElementHolder<T>>) (Codec<?>) CODEC;
	}

	public HolderSet<T> get(Registry<T> registry) {
		final var registryKey = registry.key();
		if (tag) return registry.getOrCreateTag(TagKey.create(registryKey, id));
		return HolderSet.direct(registry::getHolderOrThrow, ResourceKey.create(registryKey, id));
	}

	public String toString() {
		return this.decoratedId();
	}

	private String decoratedId() {
		return this.tag ? "#" + this.id : this.id.toString();
	}
}
