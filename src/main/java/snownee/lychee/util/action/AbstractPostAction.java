package snownee.lychee.util.action;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class AbstractPostAction<Action extends AbstractPostAction<Action>> implements PostAction<Action> {
	private @Nullable String path;

	@Override
	public Optional<String> path() {
		return Optional.ofNullable(path);
	}

	public void setPath(final String path) {
		this.path = path;
	}
}
