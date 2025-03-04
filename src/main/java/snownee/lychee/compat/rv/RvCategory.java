package snownee.lychee.compat.rv;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class RvCategory<T extends ILycheeRecipe<?>> {
	RvCategoryType<T> type;
	ResourceLocation id;
	List<RecipeHolder<T>> recipes = Lists.newArrayList();
	ScreenElement icon;

	public RvCategory(RvCategoryType<?> type, ResourceLocation id) {
		//noinspection unchecked
		this.type = (RvCategoryType<T>) type;
		this.id = id;
	}

	public void addRecipe(RecipeHolder<?> recipe) {
		//noinspection unchecked
		recipes.add((RecipeHolder<T>) recipe);
	}

	public List<ItemStack> workstations() {
		return type.workstationProvider.apply(this);
	}
}
