package snownee.lychee.compat.rei.category;

import java.util.List;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import snownee.lychee.client.gui.ScreenElement;
import snownee.lychee.compat.rei.display.BaseREIDisplay;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.item_inside.ItemInsideRecipe;
import snownee.lychee.item_inside.ItemInsideRecipeType;
import snownee.lychee.util.ClientProxy;

public class ItemInsideRecipeCategory extends ItemAndBlockBaseCategory<ItemShapelessContext, ItemInsideRecipe, BaseREIDisplay<ItemInsideRecipe>> {

	public ItemInsideRecipeCategory(ItemInsideRecipeType recipeType, ScreenElement mainIcon) {
		super(List.of(recipeType), mainIcon);
		infoRect.setPosition(4, 25);
		inputBlockRect.setX(80);
		methodRect.setX(77);
	}

	@Override
	public List<Widget> setupDisplay(BaseREIDisplay<ItemInsideRecipe> display, Rectangle bounds) {
		List<Widget> widgets = super.setupDisplay(display, bounds);
		if (display.recipe.getTime() > 0) {
			widgets.add(Widgets.createLabel(new Point(bounds.x + methodRect.getX() + 10, bounds.y + methodRect.getY() - 6), ClientProxy.format("tip.lychee.sec", display.recipe.getTime())).color(0xFF666666, 0xFFBBBBBB).noShadow().centered());
		}
		return widgets;
	}

	@Override
	public int getDisplayWidth(BaseREIDisplay<ItemInsideRecipe> display) {
		return getRealWidth();
	}

	@Override
	public int getRealWidth() {
		return width + 20;
	}

}
