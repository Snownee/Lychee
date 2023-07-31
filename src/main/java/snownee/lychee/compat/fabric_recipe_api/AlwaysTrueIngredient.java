package snownee.lychee.compat.fabric_recipe_api;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.Lychee;

public class AlwaysTrueIngredient implements CustomIngredient {
	public static final ResourceLocation ID = new ResourceLocation(Lychee.ID, "always_true");
	public static final CustomIngredientSerializer<AlwaysTrueIngredient> SERIALIZER = new CustomIngredientSerializer<>() {

		private final Supplier<AlwaysTrueIngredient> supplier = Suppliers.memoize(AlwaysTrueIngredient::new);

		@Override
		public ResourceLocation getIdentifier() {
			return ID;
		}

		@Override
		public AlwaysTrueIngredient read(JsonObject json) {
			return supplier.get();
		}

		@Override
		public void write(JsonObject json, AlwaysTrueIngredient ingredient) {
		}

		@Override
		public AlwaysTrueIngredient read(FriendlyByteBuf buf) {
			return supplier.get();
		}

		@Override
		public void write(FriendlyByteBuf buf, AlwaysTrueIngredient ingredient) {
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
