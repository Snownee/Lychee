package snownee.lychee.compat.jei.category;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import snownee.lychee.core.recipe.ILycheeRecipe;

public class CraftingRecipeCategoryExtension implements ICraftingCategoryExtension {

	private static final Rect2i infoRect = new Rect2i(67, 11, 8, 8);
	private final ILycheeRecipe<?> recipe;

	public CraftingRecipeCategoryExtension(ILycheeRecipe<?> recipe) {
		this.recipe = recipe;
	}

	@Override
	public void drawInfo(int recipeWidth, int recipeHeight, GuiGraphics graphics, double mouseX, double mouseY) {
		BaseJEICategory.drawInfoBadgeIfNeeded(recipe, graphics, mouseX, mouseY, infoRect);
	}

	@Override
	public List<Component> getTooltipStrings(double mouseX, double mouseY) {
		return BaseJEICategory.getTooltipStrings(recipe, mouseX, mouseY, infoRect);
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
		CraftingRecipe craftingRecipe = (CraftingRecipe) recipe;
		List<List<ItemStack>> inputs = craftingRecipe.getIngredients().stream().map(ingredient -> List.of(ingredient.getItems())).toList();
		ItemStack resultItem = craftingRecipe.getResultItem(Minecraft.getInstance().level.registryAccess());

		int width = getWidth();
		int height = getHeight();
		craftingGridHelper.createAndSetOutputs(builder, List.of(resultItem));
		craftingGridHelper.createAndSetInputs(builder, inputs, width, height);
	}

	@Override
	public @Nullable ResourceLocation getRegistryName() {
		return ((Recipe<?>) recipe).getId();
	}

	@Override
	public int getHeight() {
		return recipe instanceof ShapedRecipe shaped ? shaped.getHeight() : 0;
	}

	@Override
	public int getWidth() {
		return recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : 0;
	}

}
