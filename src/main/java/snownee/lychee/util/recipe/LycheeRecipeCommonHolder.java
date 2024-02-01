package snownee.lychee.util.recipe;

import java.util.List;
import java.util.Optional;

import snownee.lychee.util.action.PostAction;

public record LycheeRecipeCommonHolder(
		boolean hideInRecipeViewer,
		boolean ghost,
		Optional<String> comment,
		String group,
		List<PostAction<?>> postActions
) {}
