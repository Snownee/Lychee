package snownee.lychee;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.item.crafting.RecipeBookCategory;

public class RecipeBookCategories {
	public static final DeferredRegister<RecipeBookCategory> RECIPE_BOOK_CATEGORIES = DeferredRegister.create(BuiltInRegistries.RECIPE_BOOK_CATEGORY, Lychee.ID);
	public static final RecipeBookCategory UNLISTED = register("unlisted", new RecipeBookCategory());

	public static <T extends RecipeBookCategory> T register(String id, T serializer) {
		RECIPE_BOOK_CATEGORIES.register(id, () -> serializer);
		return serializer;
	}
}
