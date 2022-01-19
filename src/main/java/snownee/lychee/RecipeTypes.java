package snownee.lychee;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.core.Registry;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.LycheeRecipe;
import snownee.lychee.core.LycheeRecipeType;
import snownee.lychee.item_burning.ItemBurningRecipe;
import snownee.lychee.item_inside.ItemInsideRecipe;

public final class RecipeTypes {

	public static void init() {
	}

	public static final List<LycheeRecipeType<?, ?>> ALL = Lists.newLinkedList();
	public static final LycheeRecipeType<LycheeContext, ItemBurningRecipe> ITEM_BURNING = register("item_burning", ItemBurningRecipe.class);
	public static final LycheeRecipeType<LycheeContext, ItemInsideRecipe> ITEM_INSIDE = register("item_inside", ItemInsideRecipe.class);

	public static <C extends LycheeContext, T extends LycheeRecipe<C>> LycheeRecipeType<C, T> register(String name, Class<T> clazz) {
		LycheeRecipeType<C, T> recipeType = new LycheeRecipeType<>(name, clazz);
		ALL.add(recipeType);
		return Registry.register(Registry.RECIPE_TYPE, recipeType.id, recipeType);
	}

}
