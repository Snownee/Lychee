package snownee.lychee.anvil_crafting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import snownee.lychee.RecipeSerializers;
import snownee.lychee.RecipeTypes;
import snownee.lychee.core.def.IntBoundsHelper;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;
import snownee.lychee.util.json.JsonPointer;

public class AnvilCraftingRecipe extends LycheeRecipe<AnvilContext> implements Comparable<AnvilCraftingRecipe> {

	protected Ingredient left;
	protected Ingredient right = Ingredient.EMPTY;
	protected int levelCost;
	protected int materialCost;
	protected ItemStack output;

	public AnvilCraftingRecipe(ResourceLocation id) {
		super(id);
		maxRepeats = IntBoundsHelper.ONE;
	}

	@Override
	public boolean matches(AnvilContext ctx, Level pLevel) {
		if (!right.isEmpty() && ctx.right.getCount() < materialCost) {
			return false;
		}
		return left.test(ctx.left) && right.test(ctx.right);
	}

	@Override
	public ItemStack assemble(AnvilContext ctx) {
		ctx.levelCost = levelCost;
		ctx.materialCost = materialCost;
		return getResultItem();
	}

	public Ingredient getLeft() {
		return left;
	}

	public Ingredient getRight() {
		return right;
	}

	public int getMaterialCost() {
		return materialCost;
	}

	@Override
	public ItemStack getResultItem() {
		return output.copy();
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		if (right.isEmpty()) {
			return NonNullList.of(Ingredient.EMPTY, left);
		}
		return NonNullList.of(Ingredient.EMPTY, left, right);
	}

	@Override
	public IntList getItemIndexes(JsonPointer pointer) {
		if (pointer.size() == 1) {
			if (pointer.getString(0).equals("output")) {
				return IntList.of(2);
			}
			if (pointer.getString(0).equals("item_in")) {
				return right.isEmpty() ? IntList.of(0) : IntList.of(0, 1);
			}
		}
		if (pointer.size() == 2 && pointer.getString(0).equals("item_in")) {
			try {
				int i = pointer.getInt(1);
				if (i >= 0 && i < 2) {
					return IntList.of(i);
				}
			} catch (NumberFormatException e) {
			}
		}
		return IntList.of();
	}

	@Override
	public JsonPointer defaultItemPointer() {
		return OUTPUT;
	}

	@Override
	public LycheeRecipe.Serializer<?> getSerializer() {
		return RecipeSerializers.ANVIL_CRAFTING;
	}

	@Override
	public LycheeRecipeType<?, ?> getType() {
		return RecipeTypes.ANVIL_CRAFTING;
	}

	@Override
	public int compareTo(AnvilCraftingRecipe that) {
		return Integer.compare(isSpecial() ? 1 : 0, that.isSpecial() ? 1 : 0);
	}

	public static class Serializer extends LycheeRecipe.Serializer<AnvilCraftingRecipe> {

		public Serializer() {
			super(AnvilCraftingRecipe::new);
		}

		@Override
		public void fromJson(AnvilCraftingRecipe pRecipe, JsonObject pSerializedRecipe) {
			JsonElement itemIn = pSerializedRecipe.get("item_in");
			if (itemIn.isJsonArray()) {
				JsonArray array = itemIn.getAsJsonArray();
				pRecipe.left = Ingredient.fromJson(array.get(0));
				if (array.size() > 0) {
					pRecipe.right = Ingredient.fromJson(array.get(1));
				}
			} else {
				pRecipe.left = Ingredient.fromJson(itemIn);
			}
			pRecipe.output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, ("item_out")));
			pRecipe.levelCost = GsonHelper.getAsInt(pSerializedRecipe, "level_cost", 0);
			pRecipe.materialCost = GsonHelper.getAsInt(pSerializedRecipe, "material_cost", 1);
		}

		@Override
		public void fromNetwork(AnvilCraftingRecipe pRecipe, FriendlyByteBuf pBuffer) {
			pRecipe.left = Ingredient.fromNetwork(pBuffer);
			pRecipe.right = Ingredient.fromNetwork(pBuffer);
			pRecipe.output = pBuffer.readItem();
			pRecipe.levelCost = pBuffer.readVarInt();
			pRecipe.materialCost = pBuffer.readVarInt();
		}

		@Override
		public void toNetwork0(FriendlyByteBuf pBuffer, AnvilCraftingRecipe pRecipe) {
			pRecipe.left.toNetwork(pBuffer);
			pRecipe.right.toNetwork(pBuffer);
			pBuffer.writeItem(pRecipe.output);
			pBuffer.writeVarInt(pRecipe.levelCost);
			pBuffer.writeVarInt(pRecipe.materialCost);
		}

	}

}
