package snownee.lychee.core.recipe.type;

import java.util.Collection;
import java.util.Optional;
import java.util.TreeSet;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.Util;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.LycheeRecipe;

public class SortedRecipeType<C extends LycheeContext, T extends LycheeRecipe<C>> extends LycheeRecipeType<C, T> {

	@SuppressWarnings("rawtypes")
	protected final TreeSet<T> recipes = (TreeSet) Sets.newTreeSet();

	public SortedRecipeType(String name, Class<T> clazz, @Nullable LootContextParamSet paramSet) {
		super(name, clazz, paramSet);
	}

	@Override
	public void buildCache(RecipeManager recipeManager) {
		recipes.clear();
		recipes.addAll(super.recipes(recipeManager));
	}

	@Override
	public Collection<T> recipes(RecipeManager recipeManager) {
		return recipes;
	}

	public Optional<T> findFirst(C ctx, Level level) {
		return recipes.stream().flatMap($ -> {
			return Util.toStream(tryMatch($, level, ctx));
		}).findFirst();
	}

}
