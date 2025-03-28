package snownee.lychee.compat.recipeviewer.category;

import java.util.Map;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.RecipeTypes;
import snownee.lychee.compat.recipeviewer.RVHelper;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

@FunctionalInterface
public interface RvCategoryProvider<R extends ILycheeRecipe<LycheeContext>> {
	static <R extends ILycheeRecipe<LycheeContext>> RvCategoryProvider<R> get(ResourceLocation id) {
		//noinspection unchecked
		return (RvCategoryProvider<R>) ALL.get(id);
	}	Map<ResourceLocation, RvCategoryProvider<?>> ALL = new Reference2ReferenceOpenHashMap<>() {{
		register(RecipeTypes.BLOCK_CRUSHING, BlockCrushingRecipeCategory::new);

		register(RecipeTypes.BLOCK_EXPLODING, ItemAndBlockCategory::new);

		register(RecipeTypes.BLOCK_INTERACTING, BlockInteractingRecipeCategory::new);

		register(RecipeTypes.DRIPSTONE_DRIPPING, DripstoneRecipeCategory::new);

		register(RecipeTypes.LIGHTNING_CHANNELING, ItemShapelessRecipeCategory::new);

		register(RecipeTypes.ITEM_EXPLODING, ItemExplodingRecipeCategory::new);

		register(RecipeTypes.ITEM_INSIDE, ItemInsideRecipeCategory::new);

		register(RecipeTypes.ITEM_BURNING, ItemBuriningRecipeCategory::new);
	}};

	static <R extends ILycheeRecipe<LycheeContext>> RvCategoryProvider<R> register(
			LycheeRecipeType<R> recipeType,
			SimpleRvCategoryProvider<R> provider) {
		RvCategoryProvider<R> result = (type, rvHandler) ->
				id -> provider.get(type, id, rvHandler);
		ALL.put(recipeType.categoryId, result);
		return result;
	}

	Function<ResourceLocation, RvCategory<R>> get(RvCategoryType<R> type, RVHelper rvHandler);

	interface SimpleRvCategoryProvider<R extends ILycheeRecipe<LycheeContext>> {
		RvCategory<R> get(RvCategoryType<R> type, ResourceLocation id, RVHelper rvHandler);
	}


}
