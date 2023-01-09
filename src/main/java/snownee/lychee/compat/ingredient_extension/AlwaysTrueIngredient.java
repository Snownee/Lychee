package snownee.lychee.compat.ingredient_extension;

import java.io.IOException;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.faux.ingredientextension.api.ingredient.IngredientExtendable;
import com.faux.ingredientextension.api.ingredient.serializer.IIngredientSerializer;
import com.google.common.base.Suppliers;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class AlwaysTrueIngredient extends IngredientExtendable {

	private AlwaysTrueIngredient() {
		super(new ItemStack[0]);
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public boolean test(@Nullable ItemStack itemStack) {
		return true;
	}

	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return Serializer.INSTANCE;
	}

	public enum Serializer implements IIngredientSerializer<AlwaysTrueIngredient> {
		INSTANCE;

		private final Supplier<AlwaysTrueIngredient> supplier = Suppliers.memoize(AlwaysTrueIngredient::new);

		@Override
		public AlwaysTrueIngredient fromJson(JsonObject arg0) throws JsonParseException {
			return supplier.get();
		}

		@Override
		public AlwaysTrueIngredient fromNetwork(FriendlyByteBuf arg0) throws IOException {
			return supplier.get();
		}

		@Override
		public void toJson(JsonObject arg0, AlwaysTrueIngredient arg1) {
		}

		@Override
		public void toNetwork(FriendlyByteBuf arg0, AlwaysTrueIngredient arg1) throws IOException {
		}

	}

}
