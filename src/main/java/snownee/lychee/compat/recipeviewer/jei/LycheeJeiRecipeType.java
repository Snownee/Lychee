package snownee.lychee.compat.recipeviewer.jei;

import mezz.jei.api.recipe.types.IRecipeHolderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class LycheeJeiRecipeType<R extends ILycheeRecipe<LycheeContext>> implements IRecipeHolderType<R> {
	private final Identifier id;

	public LycheeJeiRecipeType(Identifier id, Class<? extends R> recipeClass) {
		this.id = id;
	}

	@Override
	public Identifier getUid() {
		return id;
	}

	@Override
	public Class<? extends RecipeHolder<R>> getRecipeClass() {
		//noinspection unchecked
		return (Class<? extends RecipeHolder<R>>) (Object) RecipeHolder.class;
	}
}
