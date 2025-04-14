package snownee.lychee.util;

import org.jetbrains.annotations.Nullable;

import snownee.lychee.util.context.LycheeContext;

public interface LycheeEntity {
	@Nullable
	LycheeContext lychee$getContext();

	void lychee$setContext(LycheeContext context);
}
