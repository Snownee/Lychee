package snownee.lychee.core;

import java.util.Collection;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import snownee.lychee.Lychee;
import snownee.lychee.mixin.RecipeManagerAccess;

public class LycheeRecipeType<C extends LycheeContext, T extends LycheeRecipe<C>> implements RecipeType<T> {
	public final ResourceLocation id;
	public final Class<? extends T> clazz;
	private boolean empty;

	public LycheeRecipeType(String name, Class<T> clazz) {
		id = new ResourceLocation(Lychee.ID, name);
		this.clazz = clazz;
	}

	@Override
	public String toString() {
		return id.toString();
	}

	public Collection<T> recipes(RecipeManager recipeManager) {
		return (Collection<T>) (Object) ((RecipeManagerAccess) recipeManager).callByType(this).values();
	}

	public void updateEmptyState(RecipeManager recipeManager) {
		empty = recipes(recipeManager).isEmpty();
	}

	public boolean isEmpty() {
		return empty;
	}
}