package snownee.lychee.compat.rei.category;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.RecipeTypes;
import snownee.lychee.compat.DisplayUtils;
import snownee.lychee.compat.rei.display.LycheeDisplay;
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

	CategoryProvider<BlockCrushingRecipe> BLOCK_CRUSHING = register(
			RecipeTypes.BLOCK_CRUSHING,
			(id, icon, recipes) -> new BlockCrushingRecipeCategory(id, icon));

	CategoryProvider<BlockExplodingRecipe> BLOCK_EXPLODING = register(
			RecipeTypes.BLOCK_EXPLODING,
			(id, icon, recipes) -> new ItemAndBlockBaseCategory<>(id, icon, RecipeTypes.BLOCK_EXPLODING) {
				{
					inputBlockRect = new Rect2i(18, 30, 20, 20);
				}

				@Override
				public void drawExtra(
						BlockExplodingRecipe recipe,
						GuiGraphics graphics,
						double mouseX,
						double mouseY,
						int centerX
				) {}
			}
	);

	CategoryProvider<BlockInteractingRecipe> BLOCK_INTERACTING = register(
			RecipeTypes.BLOCK_INTERACTING,
			(id, icon, recipes) -> new BlockInteractionRecipeCategory(id, icon)
	);

	CategoryProvider<DripstoneRecipe> DRIPSTONE = register(
			RecipeTypes.DRIPSTONE_DRIPPING,
			(id, icon, recipes) -> new DripstoneRecipeCategory(id, icon)
	);

	CategoryProvider<LightningChannelingRecipe> LIGHTNING_CHANNELING = register(
			RecipeTypes.LIGHTNING_CHANNELING,
			(id, icon, recipes) -> new ItemShapelessRecipeCategory<>(id, icon, RecipeTypes.LIGHTNING_CHANNELING)
	);

	CategoryProvider<ItemExplodingRecipe> ITEM_EXPLODING = register(
			RecipeTypes.ITEM_EXPLODING,
			(id, icon, recipes) -> new ItemShapelessRecipeCategory<>(id, icon, RecipeTypes.ITEM_EXPLODING) {
				@Override
				public void drawExtra(
						List<Widget> widgets,
						LycheeDisplay<ItemExplodingRecipe> display,
						Rectangle bounds
				) {
					Widget widget = Widgets.createDrawableWidget((GuiGraphics graphics, int mouseX, int mouseY, float delta) ->
							DisplayUtils.renderTnt(graphics, bounds.x + 89, bounds.y + 38));
					widgets.add(widget);
				}
			}
	);

	CategoryProvider<ItemInsideRecipe> ITEM_INSIDE = register(
			RecipeTypes.ITEM_INSIDE,
			(id, icon, recipes) -> new ItemInsideRecipeCategory(id, icon)
	);

	CategoryProvider<ItemBurningRecipe> ITEM_BURNING = register(
			RecipeTypes.ITEM_BURNING,
			(id, icon, recipes) -> new ItemBurningRecipeCategory(id, icon)
	);

	static <R extends ILycheeRecipe<LycheeContext>> CategoryProvider<R> get(LycheeRecipeType<LycheeContext, R> type) {
		return (CategoryProvider<R>) ALL.get(type.categoryId);
	}

	static <R extends ILycheeRecipe<LycheeContext>> CategoryProvider<R> register(
			LycheeRecipeType<LycheeContext, R> type,
			CategoryProvider<R> provider) {
		ALL.put(type.categoryId, provider);
		return provider;
	}

	@FunctionalInterface
	interface CategoryProvider<R extends ILycheeRecipe<LycheeContext>> {
		LycheeDisplayCategory<? extends LycheeDisplay<R>> get(
				CategoryIdentifier<? extends LycheeDisplay<R>> identifier,
				Renderer icon,
				Collection<RecipeHolder<R>> recipes);
	}
}
