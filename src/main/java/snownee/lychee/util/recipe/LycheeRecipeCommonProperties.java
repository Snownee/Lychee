package snownee.lychee.util.recipe;

import java.util.List;
import java.util.Optional;

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
		Optional<String> comment,
		String group,
		ContextualHolder conditions,
		List<PostAction> postActions,
		MinMaxBounds.Ints maxRepeats) {

	public static final MapCodec<Boolean> HIDE_IN_VIEWER_CODEC = ExtraCodecs.strictOptionalField(Codec.BOOL, "hide_in_viewer", false);
	public static final MapCodec<Boolean> GHOST_CODEC = ExtraCodecs.strictOptionalField(Codec.BOOL, "ghost", false);
	public static final MapCodec<Optional<String>> COMMENT_CODEC = ExtraCodecs.strictOptionalField(Codec.STRING, "comment");
	public static final MapCodec<String> GROUP_CODEC = ExtraCodecs.strictOptionalField(ExtraCodecs.validate(Codec.STRING, s -> {
		if (!ResourceLocation.isValidResourceLocation(s)) {
			return DataResult.error(() -> "Invalid group: " + s + " (must be a valid resource location)");
		}
		return DataResult.success(s);
	}), "group", ILycheeRecipe.DEFAULT_GROUP);
	public static final MapCodec<ContextualHolder> CONTEXTUAL_CODEC = ExtraCodecs.strictOptionalField(
			ContextualHolder.CODEC,
			"contextual",
			ContextualHolder.EMPTY);
	public static final MapCodec<List<PostAction>> POST_ACTION_CODEC = ExtraCodecs.strictOptionalField(
			PostActionType.LIST_CODEC,
			"post",
			List.of());
	public static final MapCodec<MinMaxBounds.Ints> MAX_REPEATS_CODEC = ExtraCodecs.strictOptionalField(
			MinMaxBounds.Ints.CODEC,
			"max_repeats",
			MinMaxBounds.Ints.ANY);
	public static final MapCodec<LycheeRecipeCommonProperties> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			HIDE_IN_VIEWER_CODEC.forGetter(LycheeRecipeCommonProperties::hideInRecipeViewer),
			GHOST_CODEC.forGetter(LycheeRecipeCommonProperties::ghost),
			COMMENT_CODEC.forGetter(LycheeRecipeCommonProperties::comment),
			GROUP_CODEC.forGetter(LycheeRecipeCommonProperties::group),
			CONTEXTUAL_CODEC.forGetter(LycheeRecipeCommonProperties::conditions),
			POST_ACTION_CODEC.forGetter(LycheeRecipeCommonProperties::postActions),
			MAX_REPEATS_CODEC.forGetter(LycheeRecipeCommonProperties::maxRepeats)).apply(instance, LycheeRecipeCommonProperties::new));
}
