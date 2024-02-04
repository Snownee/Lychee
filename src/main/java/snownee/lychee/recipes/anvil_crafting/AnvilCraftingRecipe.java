package snownee.lychee.recipes.anvil_crafting;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import net.minecraft.advancements.critereon.MinMaxBounds;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
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
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.recipe.OldLycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;
import snownee.lychee.util.json.JsonPointer;

public record AnvilCraftingRecipe implements LycheeRecipe<AnvilContext>, Comparable<AnvilCraftingRecipe> {
	private static final MinMaxBounds.Ints ONE = MinMaxBounds.Ints.exactly(1);


	protected Ingredient left;
	protected Ingredient right = Ingredient.EMPTY;
	protected int levelCost;
	protected int materialCost;
	protected ItemStack output;
	private List<PostAction<?>> assembling = Collections.emptyList();

	public AnvilCraftingRecipe(ResourceLocation id) {
		super(id);
		maxRepeats = ONE;
	}

	@Override
	public boolean matches(AnvilContext ctx, Level pLevel) {
		if (!right.isEmpty() && ctx.right.getCount() < materialCost) {
			return false;
		}
		return left.test(ctx.left) && right.test(ctx.right);
	}

	@Override
	public @NotNull ItemStack assemble(AnvilContext ctx, RegistryAccess registryAccess) {
		ctx.levelCost = levelCost;
		ctx.materialCost = materialCost;
		ctx.itemHolders.replace(2, getResultItem(registryAccess));
		ctx.enqueueActions(assembling.stream(), 1, true);
		ctx.runtime.run(this, ctx);
		return ctx.getItem(2);
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
	public @NotNull ItemStack getResultItem(RegistryAccess registryAccess) {
		return getResultItem();
	}

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
			if (pointer.getString(0).equals("item_out")) {
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
		return ITEM_OUT_POINTER;
	}

	@Override
	public OldLycheeRecipe.@NotNull Serializer<?> getSerializer() {
		return RecipeSerializers.ANVIL_CRAFTING;
	}

	@Override
	public @NotNull LycheeRecipeType<?, ?> getType() {
		return RecipeTypes.ANVIL_CRAFTING;
	}

	@Override
	public int compareTo(AnvilCraftingRecipe that) {
		return Integer.compare(isSpecial() ? 1 : 0, that.isSpecial() ? 1 : 0);
	}

	public void addAssemblingAction(PostAction action) {
		Objects.requireNonNull(action);
		if (assembling == Collections.EMPTY_LIST) {
			assembling = Lists.newArrayList();
		}
		assembling.add(action);
	}

	@Override
	public Stream<PostAction> getAllActions() {
		return Stream.concat(getPostActions(), assembling.stream());
	}

	@Override
	public boolean isActionPath(JsonPointer pointer) {
		if (pointer.isRoot())
			return false;
		String token = pointer.getString(0);
		return "assemblingActions".equals(token) || "post".equals(token);
	}

	@Override
	public Map<JsonPointer, List<PostAction<?>>> getActionGroups() {
		return Map.of(POST_POINTER, actions, new JsonPointer("/assemblingActions"), assembling);
	}

	public static class Serializer extends OldLycheeRecipe.Serializer<AnvilCraftingRecipe> {

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
			PostAction.parseActions(pSerializedRecipe.get("assemblingActions"), pRecipe::addAssemblingAction);
			pRecipe.output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(
					pSerializedRecipe,
					("item_out")
			));
			pRecipe.levelCost = GsonHelper.getAsInt(pSerializedRecipe, "level_cost", 1);
			Preconditions.checkArgument(pRecipe.levelCost > 0, "level_cost must be greater than 0");
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