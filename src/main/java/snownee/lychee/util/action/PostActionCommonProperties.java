package snownee.lychee.util.action;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import snownee.lychee.util.contextual.ContextualHolder;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class PostActionCommonProperties {
	public static final MapCodec<PostActionCommonProperties> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.STRING.optionalFieldOf("@path").forGetter(PostActionCommonProperties::getPath),
			ContextualHolder.CODEC.optionalFieldOf("contextual", ContextualHolder.EMPTY).forGetter(PostActionCommonProperties::conditions)
	).apply(instance, PostActionCommonProperties::new));
	private Optional<String> path;
	private final ContextualHolder conditions;

	public PostActionCommonProperties(Optional<String> path, ContextualHolder conditions) {
		this.path = path;
		this.conditions = conditions;
	}

	public PostActionCommonProperties() {
		this.conditions = new ContextualHolder(List.of(), null, null);
		this.path = Optional.empty();
	}

	public ContextualHolder conditions() {
		return conditions;
	}

	public Optional<String> getPath() {
		return path;
	}

	public void setPath(final @Nullable String path) {
		this.path = Optional.ofNullable(path);
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
