package snownee.lychee.util.recipe;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import snownee.lychee.util.SerializableType;

public interface LycheeRecipeSerializer<T extends ILycheeRecipe> extends RecipeSerializer<T>, SerializableType<T> {
	Ingredient EMPTY_INGREDIENT = Ingredient.of(ItemStack.EMPTY);

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
