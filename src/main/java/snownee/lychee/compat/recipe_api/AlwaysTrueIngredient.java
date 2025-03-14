package snownee.lychee.compat.recipe_api;

import java.util.List;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.kiwi.recipe.CustomIngredient;
import snownee.kiwi.recipe.CustomIngredientSerializer;
import snownee.lychee.Lychee;

public class AlwaysTrueIngredient implements CustomIngredient {
	public static final ResourceLocation ID = Lychee.id("always_true");
	public static final CustomIngredientSerializer<AlwaysTrueIngredient> SERIALIZER = new Serializer();

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
		return true;
	}

	@Override
	public CustomIngredientSerializer<?> getSerializer() {
		return SERIALIZER;
	}

	private static class Serializer implements CustomIngredientSerializer<AlwaysTrueIngredient> {
		private static final AlwaysTrueIngredient INSTANCE = new AlwaysTrueIngredient();
		public static final MapCodec<AlwaysTrueIngredient> CODEC = MapCodec.unit(INSTANCE);
		public static final StreamCodec<RegistryFriendlyByteBuf, AlwaysTrueIngredient> STREAM_CODEC = StreamCodec.unit(INSTANCE);

		@Override
		public ResourceLocation getIdentifier() {
			return ID;
		}

		@Override
		public MapCodec<AlwaysTrueIngredient> getCodec(boolean allowEmpty) {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, AlwaysTrueIngredient> getPacketCodec() {
			return STREAM_CODEC;
		}
	}
}
