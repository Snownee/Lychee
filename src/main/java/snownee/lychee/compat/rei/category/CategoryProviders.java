package snownee.lychee.compat.rei.category;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.RecipeTypes;
import snownee.lychee.compat.rei.display.LycheeDisplay;
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

public interface CategoryProviders {
	Map<ResourceLocation, CategoryProvider<?>> ALL = Maps.newHashMap();

	CategoryProvider<BlockCrushingRecipe> BLOCK_CRUSHING = register(RecipeTypes.BLOCK_CRUSHING, BlockCrushingRecipeCategory::new);

	CategoryProvider<BlockExplodingRecipe> BLOCK_EXPLODING = register(
			RecipeTypes.BLOCK_EXPLODING, (id, category) -> new ItemAndBlockBaseCategory<>(id, category) {
				{
					inputBlockRect = new Rect2i(18, 30, 20, 20);
					infoRect = new Rect2i(3, 25, 8, 8);
				}

				@Override
				public void drawExtra(BlockExplodingRecipe recipe, GuiGraphics graphics, double mouseX, double mouseY, int centerX) {}
			});

	CategoryProvider<BlockInteractingRecipe> BLOCK_INTERACTING = register(
			RecipeTypes.BLOCK_INTERACTING,
			(CategoryProvider<BlockInteractingRecipe>) BlockInteractionRecipeCategory::new);

	CategoryProvider<DripstoneRecipe> DRIPSTONE = register(
			RecipeTypes.DRIPSTONE_DRIPPING,
			(CategoryProvider<DripstoneRecipe>) DripstoneRecipeCategory::new);

	CategoryProvider<LightningChannelingRecipe> LIGHTNING_CHANNELING = register(
			RecipeTypes.LIGHTNING_CHANNELING,
			ItemShapelessRecipeCategory::new);

	CategoryProvider<ItemExplodingRecipe> ITEM_EXPLODING = register(
			RecipeTypes.ITEM_EXPLODING, (id, category) -> new ItemShapelessRecipeCategory<>(id, category) {
				@Override
				public void drawExtra(List<Widget> widgets, LycheeDisplay<ItemExplodingRecipe> display, Rectangle bounds) {
					Widget widget = Widgets.createDrawableWidget((GuiGraphics graphics, int mouseX, int mouseY, float delta) -> {
						RVs.renderTnt(graphics, bounds.x + 89, bounds.y + 38);
					});
					widgets.add(widget);
				}
			});

	CategoryProvider<ItemInsideRecipe> ITEM_INSIDE = register(
			RecipeTypes.ITEM_INSIDE,
			(CategoryProvider<ItemInsideRecipe>) ItemInsideRecipeCategory::new);

	CategoryProvider<ItemBurningRecipe> ITEM_BURNING = register(
			RecipeTypes.ITEM_BURNING,
			(CategoryProvider<ItemBurningRecipe>) ItemBurningRecipeCategory::new);

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
		AbstractLycheeCategory<R> get(CategoryIdentifier<? extends LycheeDisplay<R>> identifier, RvCategory<R> category);
	}
}
