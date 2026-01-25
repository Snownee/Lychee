package snownee.lychee.compat.recipeviewer.category;

import java.util.Map;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.resources.Identifier;
import snownee.lychee.compat.recipeviewer.RvHelper;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public final class RvCategoryInstanceProviders {
	public static final Map<Identifier, RvCategoryProvider<?>> ALL = new Reference2ReferenceOpenHashMap<>();

	public static <R extends ILycheeRecipe<LycheeContext>> RvCategoryProvider<R> register(
			LycheeRecipeType<R> recipeType,
			SimpleRvCategoryProvider<R> provider) {
		RvCategoryProvider<R> result = (type, helper) ->
				id -> provider.get(type, id, helper);
		ALL.put(recipeType.categoryId, result);
		return result;
	}

	@Nullable
	public static <R extends ILycheeRecipe<LycheeContext>> RvCategoryProvider<R> get(Identifier id) {
		if (ALL.containsKey(id)) {
			//noinspection unchecked
			return (RvCategoryProvider<R>) ALL.get(id);
		} else {
			RvCategoryProvider<R> provider = (type, helper) -> $ -> new RvCategoryInstanceImpl<>(type, $, helper);
			ALL.put(id, provider);
			return provider;
		}
	}

	@FunctionalInterface
	public interface RvCategoryProvider<R extends ILycheeRecipe<LycheeContext>> {
		Function<Identifier, RvCategoryInstance<R>> get(RvCategory<R> type, RvHelper helper);
	}

	@FunctionalInterface
	public interface SimpleRvCategoryProvider<R extends ILycheeRecipe<LycheeContext>> {
		RvCategoryInstance<R> get(RvCategory<R> type, Identifier id, RvHelper helper);
	}
}
