package snownee.lychee.util.recipe;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.util.SerializableType;

@NotNullByDefault
public interface LycheeRecipeSerializer<T extends ILycheeRecipe<?>> extends RecipeSerializer<T>, SerializableType<T> {
	@Override
	MapCodec<T> codec();

	@Override
	StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();
}
