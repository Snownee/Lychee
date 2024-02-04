package snownee.lychee.util.recipe;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import snownee.lychee.util.SerializableType;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.contextual.ConditionHolder;

public interface LycheeRecipeSerializer<T extends LycheeRecipe<T>> extends RecipeSerializer<T>, SerializableType<T> {
	Ingredient EMPTY_INGREDIENT = Ingredient.of(ItemStack.EMPTY);

	RecordCodecBuilder<LycheeRecipe<?>, Boolean> HIDE_IN_VIEWER_CODEC =
			Codec.BOOL.optionalFieldOf("hide_in_viewer", false).forGetter(LycheeRecipe::hideInRecipeViewer);

	RecordCodecBuilder<LycheeRecipe<?>, Boolean> GHOST_CODEC =
			Codec.BOOL.fieldOf("ghost").orElse(false).forGetter(LycheeRecipe::ghost);

	RecordCodecBuilder<LycheeRecipe<?>, @Nullable String> COMMENT_CODEC =
			Codec.STRING.optionalFieldOf("comment", null).forGetter(LycheeRecipe::comment);

	RecordCodecBuilder<LycheeRecipe<?>, String> GROUP_CODEC =
			Codec.STRING.fieldOf("group").orElse("default").forGetter(LycheeRecipe::group);

	RecordCodecBuilder<LycheeRecipe<?>, List<ConditionHolder<?>>> CONDITIONS_CODEC =
			ConditionHolder.LIST_CODEC.fieldOf("contextual")
									  .orElseGet(Lists::newArrayList)
									  .forGetter(LycheeRecipe::conditions);

	RecordCodecBuilder<LycheeRecipe<?>, List<? extends PostAction<?>>> POST_ACTIONS_CODEC =
			Codec.either(Codec.list(PostAction.CODEC), PostAction.CODEC)
				 .xmap(
						 it -> it.map(Function.identity(), Lists::newArrayList),
						 it -> Either.left((List<PostAction<?>>) it)
				 )
				 .fieldOf("post")
				 .orElseGet(Lists::newArrayList)
				 .forGetter(LycheeRecipe::postActions);

	RecordCodecBuilder<LycheeRecipe<?>, MinMaxBounds.Ints> MAX_REPEATS_CODEC =
			MinMaxBounds.Ints.CODEC.fieldOf("max_repeats")
								   .orElse(MinMaxBounds.Ints.ANY)
								   .forGetter(LycheeRecipe::maxRepeats);

	static <T extends LycheeRecipe<T>> RecordCodecBuilder<T, Boolean> hideInRecipeViewerCodec() {
		return (RecordCodecBuilder<T, Boolean>) HIDE_IN_VIEWER_CODEC;
	}

	static <T extends LycheeRecipe<T>> RecordCodecBuilder<T, Boolean> ghostCodec() {
		return (RecordCodecBuilder<T, Boolean>) GHOST_CODEC;
	}

	static <T extends LycheeRecipe<T>> RecordCodecBuilder<T, @Nullable String> commentCodec() {
		return (RecordCodecBuilder<T, @Nullable String>) COMMENT_CODEC;
	}

	static <T extends LycheeRecipe<T>> RecordCodecBuilder<T, String> groupCodec() {
		return (RecordCodecBuilder<T, String>) GROUP_CODEC;
	}

	static <T extends LycheeRecipe<T>> RecordCodecBuilder<T, List<ConditionHolder<?>>> conditionsCodec() {
		return (RecordCodecBuilder<T, List<ConditionHolder<?>>>) CONDITIONS_CODEC;
	}

	static <T extends LycheeRecipe<T>> RecordCodecBuilder<T, List<? extends PostAction<?>>> postActionsCodec() {
		return (RecordCodecBuilder<T, List<? extends PostAction<?>>>) POST_ACTIONS_CODEC;
	}

	static <T extends LycheeRecipe<T>> RecordCodecBuilder<T, MinMaxBounds.Ints> maxRepeatsCodec() {
		return (RecordCodecBuilder<T, MinMaxBounds.Ints>) MAX_REPEATS_CODEC;
	}

	@Override
	@NotNull
	Codec<T> codec();

	@Override
	default @NotNull T fromNetwork(FriendlyByteBuf friendlyByteBuf) {
		return SerializableType.super.fromNetwork(friendlyByteBuf);
	}

	@Override
	default void toNetwork(FriendlyByteBuf friendlyByteBuf, T recipe) {
		SerializableType.super.toNetwork(friendlyByteBuf, recipe);
	}
}
