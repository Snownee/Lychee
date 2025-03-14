package snownee.lychee.util;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import snownee.kiwi.recipe.SizedIngredient;
import snownee.kiwi.util.codec.KCodecs;
import snownee.lychee.util.codec.LycheeCodecs;

public final class IngredientCollection {
	public static final IngredientCollection EMPTY = new IngredientCollection(List.of());

	public static final Codec<IngredientCollection> CODEC = KCodecs.compactList(SizedIngredient.CODEC).xmap(
			IngredientCollection::new,
			IngredientCollection::ingredients);

	public static Codec<IngredientCollection> codec(int minSize, int maxSize) {
		return LycheeCodecs.sizeLimit(KCodecs.compactList(SizedIngredient.CODEC), minSize, maxSize).xmap(
				IngredientCollection::of,
				IngredientCollection::ingredients);
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, IngredientCollection> STREAM_CODEC = StreamCodec.composite(
			SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), IngredientCollection::ingredients, IngredientCollection::of);

	private final List<SizedIngredient> ingredients;
	private final int ingredientCount;
	private NonNullList<Ingredient> flattenedIngredients;

	public static IngredientCollection of(List<SizedIngredient> ingredients) {
		if (ingredients.isEmpty()) {
			return EMPTY;
		}
		return new IngredientCollection(ingredients);
	}

	public IngredientCollection(List<SizedIngredient> ingredients) {
		this.ingredients = ingredients;
		this.ingredientCount = ingredients.stream().mapToInt(SizedIngredient::count).sum();
	}

	public List<SizedIngredient> ingredients() {
		return ingredients;
	}

	public int size() {
		return ingredients.size();
	}

	public boolean isEmpty() {
		return ingredients.isEmpty();
	}

	public int ingredientCount() {
		return ingredientCount;
	}

	public NonNullList<Ingredient> flattenedIngredients() {
		if (flattenedIngredients == null) {
			List<Ingredient> list = Lists.newArrayListWithExpectedSize(ingredientCount);
			for (SizedIngredient ingredient : ingredients) {
				for (int i = 0; i < ingredient.count(); i++) {
					list.add(ingredient.ingredient());
				}
			}
			flattenedIngredients = NonNullListExtensions.copyOf(list);
		}
		return flattenedIngredients;
	}

	public boolean anyMatch(ItemStack itemStack) {
		for (SizedIngredient ingredient : ingredients) {
			if (ingredient.ingredient().test(itemStack)) {
				return true;
			}
		}
		return false;
	}
}
