package snownee.lychee.core.recipe;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.util.RecipeMatcher;

public abstract class ItemShapelessRecipe<T extends ItemShapelessRecipe<T>> extends LycheeRecipe<ItemShapelessContext> implements Comparable<T> {

	public static final int MAX_INGREDIENTS = 27;
	protected NonNullList<Ingredient> ingredients = NonNullList.create();

	public ItemShapelessRecipe(ResourceLocation id) {
		super(id);
	}

	@Override
	public boolean matches(ItemShapelessContext ctx, Level pLevel) {
		if (ctx.totalItems < ingredients.size()) {
			return false;
		}
		if (ingredients.isEmpty()) {
			return true;
		}
		List<ItemEntity> itemEntities = ctx.itemEntities.stream().filter($ -> {
			// ingredient.test is not thread safe
			return ingredients.stream().anyMatch(ingredient -> ingredient.test($.getItem()));
		}).limit(MAX_INGREDIENTS).toList();
		List<ItemStack> items = itemEntities.stream().map(ItemEntity::getItem).toList();
		int[] amount = items.stream().mapToInt(ItemStack::getCount).toArray();
		Optional<RecipeMatcher<ItemStack>> match = RecipeMatcher.findMatches(items, ingredients, amount);
		if (match.isEmpty()) {
			return false;
		}
		ctx.filteredItems = itemEntities;
		ctx.setMatch(match.get());
		return true;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		return ingredients;
	}

	@Override
	public int compareTo(T that) {
		int i;
		i = Integer.compare(getMaxRepeats().isAny() ? 1 : 0, that.getMaxRepeats().isAny() ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = Integer.compare(isSpecial() ? 1 : 0, that.isSpecial() ? 1 : 0);
		if (i != 0) {
			return i;
		}
		i = -Integer.compare(ingredients.size(), that.ingredients.size());
		if (i != 0) {
			return i;
		}
		return getId().compareTo(that.getId());
	}

	public static class Serializer<T extends ItemShapelessRecipe<T>> extends LycheeRecipe.Serializer<T> {

		public Serializer(Function<ResourceLocation, T> factory) {
			super(factory);
		}

		@Override
		public void fromJson(T pRecipe, JsonObject pSerializedRecipe) {
			if (pSerializedRecipe.has("item_in")) {
				JsonElement itemIn = pSerializedRecipe.get("item_in");
				if (itemIn.isJsonArray()) {
					itemIn.getAsJsonArray().forEach($ -> {
						pRecipe.ingredients.add(Ingredient.fromJson($));
					});
				} else {
					pRecipe.ingredients.add(Ingredient.fromJson(itemIn));
				}
			}
			if (!pRecipe.ghost) {
				Preconditions.checkArgument(pRecipe.ingredients.size() <= MAX_INGREDIENTS,
						"Ingredients cannot be more than %s",
						MAX_INGREDIENTS);
			}
		}

		@Override
		public void fromNetwork(T pRecipe, FriendlyByteBuf pBuffer) {
			pBuffer.readCollection(i -> pRecipe.ingredients, Ingredient::fromNetwork);
		}

		@Override
		public void toNetwork0(FriendlyByteBuf pBuffer, T pRecipe) {
			pBuffer.writeCollection(pRecipe.ingredients, (b, i) -> i.toNetwork(b));
		}

	}

}
