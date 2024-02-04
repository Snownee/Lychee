package snownee.lychee.util.recipe;

import java.util.List;
import java.util.Optional;

import net.minecraft.advancements.critereon.MinMaxBounds;
import snownee.lychee.util.action.PostAction;

public record LycheeRecipeCommonHolder(
		boolean hideInRecipeViewer,
		boolean ghost,
		Optional<String> comment,
		String group,
		List<? extends PostAction<?>> postActions,
		MinMaxBounds.Ints maxRepeats
) {}
