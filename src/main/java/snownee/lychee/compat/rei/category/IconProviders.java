package snownee.lychee.compat.rei.category;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.RecipeTypes;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public interface IconProviders {
	Map<LycheeRecipeType<LycheeContext, ?>, IconProvider> ALL = Maps.newHashMap();

	IconProvider BLOCK_CRUSHING = register(RecipeTypes.BLOCK_CRUSHING, (recipes) -> EntryStacks.of(Items.ANVIL));

	static <R extends ILycheeRecipe<LycheeContext>> IconProvider register(
			LycheeRecipeType<LycheeContext, R> recipeType,
			IconProvider renderer) {
		ALL.put(recipeType, renderer);
		return renderer;
	}

	static <R extends ILycheeRecipe<LycheeContext>> IconProvider get(LycheeRecipeType<LycheeContext, R> recipeType) {
		return ALL.get(recipeType);
	}

	@FunctionalInterface
	interface IconProvider {
		Renderer get(Collection<RecipeHolder<? extends ILycheeRecipe<LycheeContext>>> recipes);
	}
}
