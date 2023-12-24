package snownee.lychee.util.action;

import java.util.Optional;

/**
 * Using {@link PostActionPathHolder} implement {@link PostAction} path. Reduce duplicate code.
 *
 * @param <T>
 */
public interface PostActionByPathHolder<T extends PostActionByPathHolder<T>> extends PostAction<T> {
	PostActionPathHolder pathHolder();

	@Override
	default Optional<String> path() {
		return pathHolder().path();
	}

	@Override
	default void setPath(String path) {
		pathHolder().setPath(path);
	}
}
