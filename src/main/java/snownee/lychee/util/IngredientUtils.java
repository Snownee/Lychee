package snownee.lychee.util;

import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.Sets;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public final class IngredientUtils {
	public static Stream<Set<ItemStack>> flattenIngredients(NonNullList<Ingredient> ingredients) {
		return ingredients.stream()
				.map(Ingredient::getItems)
				.map(Sets::newHashSet);
	}
}
