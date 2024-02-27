package snownee.lychee.util.recipe;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import snownee.lychee.util.SerializableType;

public interface LycheeRecipeSerializer<T extends ILycheeRecipe<?>> extends RecipeSerializer<T>, SerializableType<T> {
	Ingredient EMPTY_INGREDIENT = Ingredient.of(ItemStack.EMPTY);

	@Override
	@NotNull
	Codec<T> codec();

	@Override
	@NotNull
	default StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
		return ByteBufCodecs.fromCodecWithRegistries(codec());
	}
}
