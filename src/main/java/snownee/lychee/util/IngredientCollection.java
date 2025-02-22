package snownee.lychee.util;

import java.util.List;

import com.mojang.serialization.Codec;

import snownee.kiwi.recipe_.SizedIngredient;
import snownee.kiwi.util.codec.KCodecs;
import snownee.lychee.util.codec.LycheeCodecs;

public final class IngredientCollection {
	public static final Codec<IngredientCollection> CODEC = KCodecs.compactList(SizedIngredient.CODEC).xmap(
			IngredientCollection::new,
			IngredientCollection::ingredients);

	public static Codec<IngredientCollection> codec(int minSize, int maxSize) {
		return LycheeCodecs.sizeLimit(KCodecs.compactList(SizedIngredient.CODEC), minSize, maxSize).xmap(
				IngredientCollection::new,
				IngredientCollection::ingredients);
	}

	private final List<SizedIngredient> ingredients;
	private final int ingredientCount;

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

	public int ingredientCount() {
		return ingredientCount;
	}
}
