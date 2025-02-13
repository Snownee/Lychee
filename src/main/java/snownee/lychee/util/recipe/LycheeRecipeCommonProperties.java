package snownee.lychee.util.recipe;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.util.BoundsExtensions;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionType;
import snownee.lychee.util.codec.AliasOptionalFieldCodec;
import snownee.lychee.util.codec.LycheeStreamCodecs;
import snownee.lychee.util.contextual.ContextualHolder;

public record LycheeRecipeCommonProperties(
		boolean hideInRecipeViewer,
		boolean ghost,
		Optional<String> comment,
		String group,
		ContextualHolder conditions,
		List<PostAction> postActions,
		MinMaxBounds.Ints maxRepeats) {

	public static final MapCodec<Boolean> HIDE_IN_VIEWER_CODEC = Codec.BOOL.optionalFieldOf("hide_in_viewer", false);
	public static final MapCodec<Boolean> GHOST_CODEC = Codec.BOOL.optionalFieldOf("ghost", false);
	public static final MapCodec<Optional<String>> COMMENT_CODEC = Codec.STRING.optionalFieldOf("comment");
	public static final MapCodec<String> GROUP_CODEC = Codec.STRING.validate(s -> {
		if (ResourceLocation.tryParse(s) == null) {
			return DataResult.error(() -> "Invalid group: " + s + " (must be a valid resource location)");
		}
		return DataResult.success(s);
	}).optionalFieldOf("group", ILycheeRecipe.DEFAULT_GROUP);
	public static final MapCodec<ContextualHolder> CONTEXTUAL_CODEC = AliasOptionalFieldCodec.defaulted(
			"if",
			ContextualHolder.CODEC,
			ContextualHolder.EMPTY,
			"contextual");
	public static final MapCodec<List<PostAction>> POST_ACTION_CODEC = PostActionType.LIST_CODEC.optionalFieldOf("post", List.of());
	public static final MapCodec<MinMaxBounds.Ints> MAX_REPEATS_CODEC =
			MinMaxBounds.Ints.CODEC.optionalFieldOf("max_repeats", MinMaxBounds.Ints.ANY);
	public static final MapCodec<LycheeRecipeCommonProperties> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			HIDE_IN_VIEWER_CODEC.forGetter(LycheeRecipeCommonProperties::hideInRecipeViewer),
			GHOST_CODEC.forGetter(LycheeRecipeCommonProperties::ghost),
			COMMENT_CODEC.forGetter(LycheeRecipeCommonProperties::comment),
			GROUP_CODEC.forGetter(LycheeRecipeCommonProperties::group),
			CONTEXTUAL_CODEC.forGetter(LycheeRecipeCommonProperties::conditions),
			POST_ACTION_CODEC.forGetter(LycheeRecipeCommonProperties::postActions),
			MAX_REPEATS_CODEC.forGetter(LycheeRecipeCommonProperties::maxRepeats)).apply(instance, LycheeRecipeCommonProperties::of));

	public static final StreamCodec<RegistryFriendlyByteBuf, LycheeRecipeCommonProperties> STREAM_CODEC =
			LycheeStreamCodecs.composite(
					ByteBufCodecs.BOOL,
					LycheeRecipeCommonProperties::hideInRecipeViewer,
					ByteBufCodecs.BOOL,
					LycheeRecipeCommonProperties::ghost,
					ByteBufCodecs.fromCodec(COMMENT_CODEC.codec()),
					LycheeRecipeCommonProperties::comment,
					ByteBufCodecs.STRING_UTF8,
					LycheeRecipeCommonProperties::group,
					ByteBufCodecs.fromCodec(CONTEXTUAL_CODEC.codec()),
					LycheeRecipeCommonProperties::conditions,
					PostActionType.STREAM_LIST_CODEC,
					LycheeRecipeCommonProperties::postActions,
					ByteBufCodecs.fromCodec(MinMaxBounds.Ints.CODEC),
					LycheeRecipeCommonProperties::maxRepeats,
					LycheeRecipeCommonProperties::new
			);

	private static LycheeRecipeCommonProperties of(
			boolean hideInRecipeViewer,
			boolean ghost,
			Optional<String> comment,
			String group,
			ContextualHolder conditions,
			List<PostAction> postActions,
			MinMaxBounds.Ints maxRepeats) {
		if (postActions.stream().anyMatch(it -> !it.repeatable())) {
			maxRepeats = BoundsExtensions.ONE;
		}
		return new LycheeRecipeCommonProperties(hideInRecipeViewer, ghost, comment, group, conditions, postActions, maxRepeats);
	}
}
