package snownee.lychee.util.recipe;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.advancements.critereon.MinMaxBounds;
import snownee.lychee.util.action.PostAction;

public record LycheeRecipeCommonHolder(
		boolean hideInRecipeViewer,
		boolean ghost,
		@Nullable String comment,
		String group,
		List<PostAction<?>> postActions,
		MinMaxBounds.Ints maxRepeats
) {}
