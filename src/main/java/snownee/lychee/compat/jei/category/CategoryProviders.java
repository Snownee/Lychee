package snownee.lychee.compat.jei.category;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.RecipeTypes;
import snownee.lychee.compat.rv.RVs;
import snownee.lychee.compat.rv.RvCategory;
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
			(recipeType, category, guiHelper) -> new BlockCrushingRecipeCategory(recipeType, category));

	CategoryProvider<BlockExplodingRecipe> BLOCK_EXPLODING = register(
			RecipeTypes.BLOCK_EXPLODING,
			(recipeType, category, guiHelper) -> new ItemAndBlockBaseCategory<>(recipeType, category) {
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
			(recipeType, category, guiHelper) -> new BlockInteractionRecipeCategory(recipeType, category));

	CategoryProvider<DripstoneRecipe> DRIPSTONE = register(
			RecipeTypes.DRIPSTONE_DRIPPING,
			(recipeType, category, guiHelper) -> new DripstoneRecipeCategory(recipeType, category));

	CategoryProvider<LightningChannelingRecipe> LIGHTNING_CHANNELING = register(
			RecipeTypes.LIGHTNING_CHANNELING,
			(recipeType, category, guiHelper) -> new ItemShapelessRecipeCategory<>(recipeType, category));

	CategoryProvider<ItemExplodingRecipe> ITEM_EXPLODING = register(
			RecipeTypes.ITEM_EXPLODING,
			(recipeType, category, guiHelper) -> new ItemShapelessRecipeCategory<>(recipeType, category) {
				@Override
				public void draw(
						RecipeHolder<ItemExplodingRecipe> recipe,
						IRecipeSlotsView recipeSlotsView,
						GuiGraphics graphics,
						double mouseX,
						double mouseY) {
					RVs.renderTnt(graphics, 85, 34);
				}
			});

	CategoryProvider<ItemInsideRecipe> ITEM_INSIDE = register(
			RecipeTypes.ITEM_INSIDE,
			(recipeType, category, guiHelper) -> new ItemInsideRecipeCategory(recipeType, category));

	CategoryProvider<ItemBurningRecipe> ITEM_BURNING = register(
			RecipeTypes.ITEM_BURNING,
			(recipeType, category, guiHelper) -> new ItemBurningRecipeCategory(recipeType, category));

	@Nullable
	static <R extends ILycheeRecipe<LycheeContext>> CategoryProvider<R> get(ResourceLocation id) {
		//noinspection unchecked
		return (CategoryProvider<R>) ALL.get(id);
	}

	static <R extends ILycheeRecipe<LycheeContext>> CategoryProvider<R> register(LycheeRecipeType<R> type, CategoryProvider<R> provider) {
		ALL.put(type.categoryId, provider);
		return provider;
	}

	@FunctionalInterface
	interface CategoryProvider<R extends ILycheeRecipe<LycheeContext>> {
		AbstractLycheeCategory<R> get(RecipeType<RecipeHolder<R>> recipeType, RvCategory<R> category, IGuiHelper guiHelper);
	}
}
