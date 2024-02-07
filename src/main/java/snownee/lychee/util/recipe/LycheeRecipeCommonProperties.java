package snownee.lychee.util.recipe;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.contextual.ContextualHolder;

public record LycheeRecipeCommonProperties(
		boolean hideInRecipeViewer,
		boolean ghost,
		@Nullable String comment,
		String group,
		ContextualHolder conditions,
		List<PostAction<?>> postActions,
		MinMaxBounds.Ints maxRepeats
) {
	public static final MapCodec<LycheeRecipeCommonProperties> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.BOOL.optionalFieldOf("hide_in_viewer", false).forGetter(LycheeRecipeCommonProperties::hideInRecipeViewer),
			Codec.BOOL.optionalFieldOf("ghost", false).forGetter(LycheeRecipeCommonProperties::ghost),
			Codec.STRING.optionalFieldOf("comment", null).forGetter(LycheeRecipeCommonProperties::comment),
			ExtraCodecs.validate(Codec.STRING, s -> {
				if (!ResourceLocation.isValidResourceLocation(s)) {
					return DataResult.error(() -> "Invalid group: " + s + " (must be a valid resource location)");
				}
				return DataResult.success(s);
			}).optionalFieldOf("group", ILycheeRecipe.DEFAULT_GROUP).forGetter(LycheeRecipeCommonProperties::group),
			ContextualHolder.CODEC.optionalFieldOf("contextual", ContextualHolder.EMPTY).forGetter(LycheeRecipeCommonProperties::conditions),
			PostActionType.LIST_CODEC.optionalFieldOf("post", List.of()).forGetter(LycheeRecipeCommonProperties::postActions),
			MinMaxBounds.Ints.CODEC.optionalFieldOf("max_repeats", MinMaxBounds.Ints.ANY).forGetter(LycheeRecipeCommonProperties::maxRepeats)
	).apply(instance, LycheeRecipeCommonProperties::new));
}
