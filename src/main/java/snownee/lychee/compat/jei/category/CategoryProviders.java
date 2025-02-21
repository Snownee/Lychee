package snownee.lychee.compat.jei.category;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.RecipeTypes;
import snownee.lychee.compat.JEIREI;
import snownee.lychee.recipes.BlockCrushingRecipe;
import snownee.lychee.recipes.BlockExplodingRecipe;
import snownee.lychee.recipes.BlockInteractingRecipe;
import snownee.lychee.recipes.DripstoneRecipe;
import snownee.lychee.recipes.ItemBurningRecipe;
import snownee.lychee.recipes.ItemExplodingRecipe;
import snownee.lychee.recipes.ItemInsideRecipe;
import snownee.lychee.recipes.LightningChannelingRecipe;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

@NotNullByDefault
public interface CategoryProviders {
	Map<ResourceLocation, CategoryProvider<?>> ALL = Maps.newHashMap();

	CategoryProvider<BlockCrushingRecipe> BLOCK_CRUSHING = register(
			RecipeTypes.BLOCK_CRUSHING,
			(recipeType, icon, recipes, guiHelper) -> new BlockCrushingRecipeCategory(recipeType, icon));

	CategoryProvider<BlockExplodingRecipe> BLOCK_EXPLODING = register(
			RecipeTypes.BLOCK_EXPLODING,
			(recipeType, icon, recipes, guiHelper) -> new ItemAndBlockBaseCategory<>(recipeType, icon, RecipeTypes.BLOCK_EXPLODING) {
				{
					inputBlockRect = new Rect2i(15, 30, 20, 20);
					infoRect.setPosition(0, 25);
				}

				@Override
				public void drawExtra(
						RecipeHolder<BlockExplodingRecipe> recipe,
						GuiGraphics graphics,
						double mouseX,
						double mouseY,
						int centerX) {}
			});

	CategoryProvider<BlockInteractingRecipe> BLOCK_INTERACTING = register(
			RecipeTypes.BLOCK_INTERACTING,
			(recipeType, icon, recipes, guiHelper) -> new BlockInteractionRecipeCategory(recipeType, icon));

	CategoryProvider<DripstoneRecipe> DRIPSTONE = register(
			RecipeTypes.DRIPSTONE_DRIPPING,
			(recipeType, icon, recipes, guiHelper) -> new DripstoneRecipeCategory(recipeType, icon));

	CategoryProvider<LightningChannelingRecipe> LIGHTNING_CHANNELING = register(
			RecipeTypes.LIGHTNING_CHANNELING,
			(recipeType, icon, recipes, guiHelper) -> new ItemShapelessRecipeCategory<>(
					recipeType,
					icon,
					RecipeTypes.LIGHTNING_CHANNELING));

	CategoryProvider<ItemExplodingRecipe> ITEM_EXPLODING = register(
			RecipeTypes.ITEM_EXPLODING,
			(recipeType, icon, recipes, guiHelper) -> new ItemShapelessRecipeCategory<>(recipeType, icon, RecipeTypes.ITEM_EXPLODING) {
				@Override
				public void draw(
						RecipeHolder<ItemExplodingRecipe> recipe,
						IRecipeSlotsView recipeSlotsView,
						GuiGraphics graphics,
						double mouseX,
						double mouseY) {
					drawInfoBadgeIfNeeded(graphics, recipe.value(), mouseX, mouseY);
					JEIREI.renderTnt(graphics, 85, 34);
				}
			});

	CategoryProvider<ItemInsideRecipe> ITEM_INSIDE = register(
			RecipeTypes.ITEM_INSIDE,
			(recipeType, icon, recipes, guiHelper) -> new ItemInsideRecipeCategory(recipeType, icon));

	CategoryProvider<ItemBurningRecipe> ITEM_BURNING = register(
			RecipeTypes.ITEM_BURNING,
			(recipeType, icon, recipes, guiHelper) -> new ItemBurningRecipeCategory(recipeType, icon));

	@Nullable
	static <R extends ILycheeRecipe<LycheeContext>> CategoryProvider<R> get(LycheeRecipeType<R> type) {
		return (CategoryProvider<R>) ALL.get(type.categoryId);
	}

	static <R extends ILycheeRecipe<LycheeContext>> CategoryProvider<R> register(LycheeRecipeType<R> type, CategoryProvider<R> provider) {
		ALL.put(type.categoryId, provider);
		return provider;
	}

	@FunctionalInterface
	interface CategoryProvider<R extends ILycheeRecipe<LycheeContext>> {
		AbstractLycheeCategory<R> get(
				RecipeType<RecipeHolder<R>> recipeType,
				IDrawable icon,
				List<RecipeHolder<R>> recipes,
				IGuiHelper guiHelper);
	}
}
