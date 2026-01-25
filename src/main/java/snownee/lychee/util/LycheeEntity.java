package snownee.lychee.util;

import org.jspecify.annotations.Nullable;

import snownee.lychee.util.context.LycheeContext;

public interface LycheeEntity {
	@Nullable
	LycheeContext lychee$getContext();

	void lychee$setContext(LycheeContext context);
}
