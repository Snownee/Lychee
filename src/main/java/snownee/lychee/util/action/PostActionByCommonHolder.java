package snownee.lychee.util.action;

import java.util.Optional;

/**
 * Using {@link PostActionCommonHolder} implement {@link PostAction} path. Reduce duplicate code.
 *
 * @param <T>
 */
public interface PostActionByCommonHolder<T extends PostActionByCommonHolder<T>> extends PostAction<T> {
	PostActionCommonHolder postCommonHolder();

	@Override
	default Optional<String> getPath() {
		return postCommonHolder().getPath();
	}

	@Override
	default void setPath(String path) {
		postCommonHolder().setPath(path);
	}
}
