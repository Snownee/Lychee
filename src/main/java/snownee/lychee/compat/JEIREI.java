package snownee.lychee.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.MutableTriple;

import com.google.common.collect.Lists;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;
import snownee.lychee.core.post.PostAction;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.util.LUtil;

public class JEIREI {

	public static List<MutableTriple<Ingredient, Component, Integer>> generateInputs(LycheeRecipe<?> recipe) {
		/* off */
		List<MutableTriple<Ingredient, Component, Integer>> ingredients = recipe.getIngredients()
				.stream()
				.map($ -> MutableTriple.of($, (Component) null, 1))
				.collect(Collectors.toCollection(ArrayList::new));
		/* on */
		for (PostAction action : recipe.getPostActions()) {
			action.loadCatalystsInfo(recipe, ingredients);
		}
		if (!recipe.getType().compactInputs) {
			return ingredients;
		}
		List<MutableTriple<Ingredient, Component, Integer>> newIngredients = Lists.newArrayList();
		for (var ingredient : ingredients) {
			MutableTriple<Ingredient, Component, Integer> match = null;
			if (LUtil.isSimpleIngredient(ingredient.left)) {
				for (var toCompare : newIngredients) {
					if (toCompare.middle == ingredient.middle && LUtil.isSimpleIngredient(toCompare.left) && toCompare.left.getStackingIds().equals(ingredient.left.getStackingIds())) {
						match = toCompare;
						break;
					}
				}
			}
			if (match == null) {
				newIngredients.add(ingredient);
			} else {
				match.setRight(match.right + 1);
			}
		}
		return newIngredients;
	}

}
