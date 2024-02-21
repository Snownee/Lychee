package snownee.lychee.util.action;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class PostActionCommonProperties {
	private @Nullable String path;

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
