package snownee.lychee.recipes.interaction;

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.LycheeRecipeContext;
import snownee.lychee.core.def.BlockPredicateHelper;
import snownee.lychee.core.def.IntBoundsHelper;
import snownee.lychee.util.recipe.ItemAndBlockRecipe;
import snownee.lychee.util.recipe.OldLycheeRecipe;
import snownee.lychee.util.recipe.type.LycheeRecipeType;

public class BlockInteractingRecipe extends ItemAndBlockRecipe<LycheeRecipeContext> {

	protected Ingredient otherInput = Ingredient.EMPTY;

	public BlockInteractingRecipe(ResourceLocation id) {
		super(id);
		maxRepeats = IntBoundsHelper.ONE;
	}

	@Override
	public boolean matches(LycheeRecipeContext ctx, Level pLevel) {
		if (!super.matches(ctx, pLevel)) {
			return false;
		}
		return otherInput.isEmpty() || otherInput.test(ctx.getItem(1));
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		if (otherInput.isEmpty()) {
			return super.getIngredients();
		}
		return NonNullList.of(Ingredient.EMPTY, input, otherInput);
	}

	@Override
	public OldLycheeRecipe.@NotNull Serializer<?> getSerializer() {
		return RecipeSerializers.BLOCK_INTERACTING;
	}

	@Override
	public @NotNull LycheeRecipeType<?, ?> getType() {
		return RecipeTypes.BLOCK_INTERACTING;
	}

	public static class Serializer<T extends BlockInteractingRecipe> extends ItemAndBlockRecipe.Serializer<T> {

		public Serializer(Function<ResourceLocation, T> factory) {
			super(factory);
		}

		@Override
		public void fromJson(T pRecipe, JsonObject pSerializedRecipe) {
			JsonElement element = pSerializedRecipe.get("item_in");
			if (element.isJsonObject()) {
				pRecipe.input = parseIngredientOrAir(pSerializedRecipe.get("item_in"));
			} else {
				JsonArray array = element.getAsJsonArray();
				Preconditions.checkArgument(array.size() <= 2, "Too many items in item_in");
				pRecipe.input = parseIngredientOrAir(array.get(0));
				if (array.size() == 2) {
					pRecipe.otherInput = parseIngredientOrAir(array.get(1));
				}
			}
			pRecipe.block = BlockPredicateHelper.fromJson(pSerializedRecipe.get("block_in"));
		}

		@Override
		public void fromNetwork(T pRecipe, FriendlyByteBuf pBuffer) {
			super.fromNetwork(pRecipe, pBuffer);
			pRecipe.otherInput = Ingredient.fromNetwork(pBuffer);
		}

		@Override
		public void toNetwork0(FriendlyByteBuf pBuffer, T pRecipe) {
			super.toNetwork0(pBuffer, pRecipe);
			pRecipe.otherInput.toNetwork(pBuffer);
		}
	}
}
