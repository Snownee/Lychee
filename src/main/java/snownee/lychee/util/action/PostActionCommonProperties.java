package snownee.lychee.util.action;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class PostActionCommonProperties {
	public static final MapCodec<PostActionCommonProperties> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.STRING.optionalFieldOf("path", null).forGetter(it -> it.path)
	).apply(instance, PostActionCommonProperties::new));
	private @Nullable String path;

	public PostActionCommonProperties(@Nullable String path) {
		this.path = path;
	}

	public Optional<String> getPath() {
		return Optional.ofNullable(path);
	}

	public void setPath(final @Nullable String path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("path", path)
				.toString();
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final PostActionCommonProperties that = (PostActionCommonProperties) o;
		return Objects.equal(path, that.path);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(path);
	}
}
