package snownee.lychee.compat.fabric_recipe_api;

import java.util.List;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.Lychee;

public class AlwaysTrueIngredient implements CustomIngredient {
	public static final ResourceLocation ID = new ResourceLocation(Lychee.ID, "always_true");
	public static final CustomIngredientSerializer<AlwaysTrueIngredient> SERIALIZER =
			new CustomIngredientSerializer<>() {

				private static final AlwaysTrueIngredient INSTANCE = new AlwaysTrueIngredient();
				public static final Codec<AlwaysTrueIngredient> CODEC = Codec.unit(INSTANCE);
				public static final StreamCodec<RegistryFriendlyByteBuf, AlwaysTrueIngredient> STREAM_CODEC = StreamCodec.unit(INSTANCE);

				@Override
				public ResourceLocation getIdentifier() {
					return ID;
				}

				@Override
				public Codec<AlwaysTrueIngredient> getCodec(boolean allowEmpty) {
					return CODEC;
				}

				@Override
				public StreamCodec<RegistryFriendlyByteBuf, AlwaysTrueIngredient> getPacketCodec() {
					return STREAM_CODEC;
				}
			};

	@Override
	public boolean test(ItemStack stack) {
		return true;
	}

	@Override
	public List<ItemStack> getMatchingStacks() {
		return List.of();
	}

	@Override
	public boolean requiresTesting() {
		return false;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}
}
