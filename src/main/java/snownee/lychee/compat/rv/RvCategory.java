package snownee.lychee.compat.rv;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.client.gui.RenderElement;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class RvCategory<T extends ILycheeRecipe<?>> {
	public final RvCategoryType<T> type;
	public final ResourceLocation id;
	public final List<RecipeHolder<T>> recipes = Lists.newArrayList();
	private RenderElement icon;

	public RvCategory(RvCategoryType<?> type, ResourceLocation id) {
		//noinspection unchecked
		this.type = (RvCategoryType<T>) type;
		this.id = id;
	}

	public void addRecipe(RecipeHolder<?> recipe) {
		//noinspection unchecked
		recipes.add((RecipeHolder<T>) recipe);
	}

	public List<List<ItemStack>> workstations() {
		return type.workstationProvider == null ? List.of() : type.workstationProvider.apply(this);
	}

	public RenderElement icon() {
		if (icon == null) {
			icon = type.iconProvider.apply(this).map(RenderElement::of, GuiGameElement::of);
		}
		return icon;
	}
}
