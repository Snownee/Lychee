package snownee.lychee.compat.rei.category;

import java.util.List;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import snownee.lychee.RecipeTypes;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.compat.rei.display.ItemInsideDisplay;
import snownee.lychee.core.LycheeContext;
import snownee.lychee.core.recipe.ItemAndBlockRecipe;
import snownee.lychee.core.recipe.type.BlockKeyRecipeType;
import snownee.lychee.item_inside.ItemInsideRecipe;
import snownee.lychee.util.LUtil;

public class ItemInsideRecipeCategory<T extends ItemAndBlockRecipe<LycheeContext>> extends ItemAndBlockBaseCategory<LycheeContext, T, ItemInsideDisplay<T>> {

	public ItemInsideRecipeCategory(BlockKeyRecipeType<LycheeContext, T> recipeType, ScreenElement mainIcon) {
		this(List.of(recipeType), mainIcon);
	}

	@SuppressWarnings("rawtypes")
	public ItemInsideRecipeCategory(List<BlockKeyRecipeType<LycheeContext, T>> recipeTypes, ScreenElement mainIcon) {
		super((List) recipeTypes, mainIcon);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public CategoryIdentifier<? extends ItemInsideDisplay<T>> getCategoryIdentifier() {
		return (CategoryIdentifier) REICompat.ITEM_INSIDE;
	}

	@Override
	public List<Widget> setupDisplay(ItemInsideDisplay<T> display, Rectangle bounds) {
		List<Widget> widgets = super.setupDisplay(display, bounds);
		if (display.recipe.getType() == RecipeTypes.ITEM_INSIDE) {
			ItemInsideRecipe insideRecipe = (ItemInsideRecipe) display.recipe;
			if (insideRecipe.getTime() > 0) {
				widgets.add(Widgets.createLabel(new Point(bounds.x + 6, bounds.y + 6), LUtil.format("tip.lychee.sec", insideRecipe.getTime())).color(0xFF404040, 0xFFBBBBBB).noShadow().leftAligned());
			}
		}
		return widgets;
	}

}
