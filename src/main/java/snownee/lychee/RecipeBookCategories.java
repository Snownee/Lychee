package snownee.lychee;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeBookCategory;

public class RecipeBookCategories {
	public static final RecipeBookCategory UNLISTED = register("unlisted", new RecipeBookCategory());

	public static <T extends RecipeBookCategory> T register(String id, T serializer) {
		return Registry.register(BuiltInRegistries.RECIPE_BOOK_CATEGORY, Lychee.id(id), serializer);
	}
}
