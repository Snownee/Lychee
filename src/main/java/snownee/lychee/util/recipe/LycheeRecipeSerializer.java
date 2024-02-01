package snownee.lychee.util.recipe;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import snownee.lychee.util.SerializableType;

public interface LycheeRecipeSerializer<T extends LycheeRecipe<T>> extends RecipeSerializer<T>, SerializableType<T> {
	Ingredient EMPTY_INGREDIENT = Ingredient.of(ItemStack.EMPTY);

	RecordCodecBuilder<LycheeRecipe<?>, Boolean> HIDE_IN_VIEWER_CODEC =
			Codec.BOOL.fieldOf("hide_in_viewer").orElse(false).forGetter(LycheeRecipe::hideInRecipeViewer);

	RecordCodecBuilder<LycheeRecipe<?>, Boolean> GHOST_CODEC =
			Codec.BOOL.fieldOf("ghost").orElse(false).forGetter(LycheeRecipe::ghost);

	RecordCodecBuilder<LycheeRecipe<?>, Optional<String>> COMMENT_CODEC =
			Codec.STRING.optionalFieldOf("comment").forGetter(LycheeRecipe::comment);

	RecordCodecBuilder<LycheeRecipe<?>, String> GROUP_CODEC =
			Codec.STRING.fieldOf("group").orElse("default").forGetter(LycheeRecipe::group);

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
