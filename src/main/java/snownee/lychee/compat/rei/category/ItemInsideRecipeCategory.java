package snownee.lychee.compat.rei.category;

import java.util.List;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.rei.REICompat;
import snownee.lychee.compat.rei.display.ItemInsideDisplay;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.item_inside.ItemInsideRecipe;
import snownee.lychee.item_inside.ItemInsideRecipeType;
import snownee.lychee.util.LUtil;

public class ItemInsideRecipeCategory extends ItemAndBlockBaseCategory<ItemShapelessContext, ItemInsideRecipe, ItemInsideDisplay> {

	public ItemInsideRecipeCategory(ItemInsideRecipeType recipeType, ScreenElement mainIcon) {
		super(List.of(recipeType), mainIcon);
		infoRect.setPosition(4, 25);
		inputBlockRect.setX(80);
		methodRect.setX(77);
	}

	@Override
	public CategoryIdentifier<? extends ItemInsideDisplay> getCategoryIdentifier() {
		return REICompat.ITEM_INSIDE;
	}

	@Override
	public List<Widget> setupDisplay(ItemInsideDisplay display, Rectangle bounds) {
		List<Widget> widgets = super.setupDisplay(display, bounds);
		if (display.recipe.getTime() > 0) {
			widgets.add(Widgets.createLabel(new Point(bounds.x + methodRect.getX() + 10, bounds.y + methodRect.getY() - 6), LUtil.format("tip.lychee.sec", display.recipe.getTime())).color(0xFF666666, 0xFFBBBBBB).noShadow().centered());
		}
		return widgets;
	}

	@Override
	public int getDisplayWidth(ItemInsideDisplay display) {
		return getRealWidth();
	}

	@Override
	public int getRealWidth() {
		return width + 20;
	}

}
