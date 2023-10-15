package snownee.lychee.compat.forge;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;

public class AlwaysTrueIngredient extends AbstractIngredient {

	private AlwaysTrueIngredient() {
		super();
	}

	@SuppressWarnings("null")
	@Override
	public boolean test(@Nullable ItemStack itemStack) {
		return true;
	}

	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return Serializer.INSTANCE;
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public JsonElement toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("type", CraftingHelper.getID(Serializer.INSTANCE).toString());
		return json;
	}

	public enum Serializer implements IIngredientSerializer<AlwaysTrueIngredient> {
		INSTANCE;

		private final Supplier<AlwaysTrueIngredient> supplier = Suppliers.memoize(AlwaysTrueIngredient::new);

		@Override
		public AlwaysTrueIngredient parse(FriendlyByteBuf buffer) {
			return supplier.get();
		}

		@Override
		public AlwaysTrueIngredient parse(JsonObject json) {
			return supplier.get();
		}

		@Override
		public void write(FriendlyByteBuf buffer, AlwaysTrueIngredient ingredient) {
		}

	}

}
