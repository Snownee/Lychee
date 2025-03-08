package snownee.lychee.compat.rei.category;

import java.util.List;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.compat.rv.RvCategory;
import snownee.lychee.recipes.ItemInsideRecipe;
import snownee.lychee.util.ClientProxy;

public class ItemInsideRecipeCategory extends ItemAndBlockBaseCategory<ItemInsideRecipe> {

	public ItemInsideRecipeCategory(
			CategoryIdentifier<? extends LycheeDisplay<ItemInsideRecipe>> id,
			RvCategory<ItemInsideRecipe> category) {
		super(id, category);
		infoRect.setPosition(4, 25);
		inputBlockRect.setX(80);
		methodRect.setX(77);
	}

	@Override
	public List<Widget> setupDisplay(LycheeDisplay<ItemInsideRecipe> display, Rectangle bounds) {
		List<Widget> widgets = super.setupDisplay(display, bounds);
		if (display.recipe().time() > 0) {
			widgets.add(Widgets.createLabel(
					new Point(bounds.x + methodRect.getX() + 10, bounds.y + methodRect.getY() - 6),
					ClientProxy.format("tip.lychee.sec", display.recipe().time())).color(0xFF666666, 0xFFBBBBBB).noShadow().centered());
		}
		return widgets;
	}

	@Override
	public int getDisplayWidth(LycheeDisplay<ItemInsideRecipe> display) {
		return contentWidth();
	}

	@Override
	public int contentWidth() {
		return WIDTH + 20;
	}

	@Override
	protected void renderIngredientGroup(List<Widget> widgets, Point startPoint, ItemInsideRecipe recipe, int y) {
		ingredientGroup(widgets, startPoint, recipe, 40, y);
	}
}
