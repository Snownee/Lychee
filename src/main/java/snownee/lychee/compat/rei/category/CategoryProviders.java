package snownee.lychee.compat.rei.category;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.RecipeTypes;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public interface CategoryProviders {
	Map<ResourceLocation, CategoryProvider<?>> ALL = Maps.newHashMap();

	CategoryProvider<BlockCrushingRecipe> BLOCK_CRUSHING = register(
			RecipeTypes.BLOCK_CRUSHING.categoryId,
			(id, icon, recipes) -> new BlockCrushingRecipeCategory(id, icon));

	static <R extends ILycheeRecipe<LycheeContext>> CategoryProvider<R> get(LycheeRecipeType<LycheeContext, R> type) {
		return (CategoryProvider<R>) ALL.get(type.categoryId);
	}

	static <R extends ILycheeRecipe<LycheeContext>> CategoryProvider<R> register(
			ResourceLocation type,
			CategoryProvider<R> provider) {
		ALL.put(type, provider);
		return provider;
	}

	@FunctionalInterface
	interface CategoryProvider<R extends ILycheeRecipe<LycheeContext>> {
		LycheeDisplayCategory<? extends LycheeDisplay<R>> get(
				CategoryIdentifier<? extends LycheeDisplay<R>> identifier,
				Renderer icon,
				Collection<RecipeHolder<R>> recipes);
	}
}
