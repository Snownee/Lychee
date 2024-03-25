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
import snownee.lychee.compat.rei.display.SimpleLycheeDisplay;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.recipes.LightningChannelingRecipe;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public interface CategoryProviders {
	Map<ResourceLocation, CategoryProvider<?>> ALL = Maps.newHashMap();

	CategoryProvider<BlockCrushingRecipe> BLOCK_CRUSHING = register(
			RecipeTypes.BLOCK_CRUSHING,
			(id, icon, recipes) -> new BlockCrushingRecipeCategory(id, icon));

	CategoryProvider<LightningChannelingRecipe> LIGHTNING_CHANNELING = register(
			RecipeTypes.LIGHTNING_CHANNELING,
			(id, icon, recipes) -> new ItemShapelessRecipeCategory<LightningChannelingRecipe, SimpleLycheeDisplay<LightningChannelingRecipe>>(
					id,
					icon) {}
	);

	static <R extends ILycheeRecipe<LycheeContext>> CategoryProvider<R> get(LycheeRecipeType<LycheeContext, R> type) {
		return (CategoryProvider<R>) ALL.get(type.categoryId);
	}

	static <R extends ILycheeRecipe<LycheeContext>> CategoryProvider<R> register(
			LycheeRecipeType<LycheeContext, R> type,
			CategoryProvider<R> provider) {
		ALL.put(type.categoryId, provider);
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
