package snownee.lychee.compat.recipeviewer.rei.category;

import java.util.List;

import com.google.common.collect.Lists;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import net.minecraft.client.gui.GuiGraphics;
import snownee.lychee.compat.recipeviewer.RvCategory;
import snownee.lychee.compat.recipeviewer.rei.display.LycheeDisplay;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;

public class ItemShapelessRecipeCategory<T extends ILycheeRecipe<LycheeContext>> extends AbstractLycheeCategory<T> {

	public ItemShapelessRecipeCategory(RvCategory<T> category) {
		super(category);
	}

	@Override
	public List<Widget> setupDisplay(LycheeDisplay<T> display, Rectangle bounds) {
		var startPoint = new Point(bounds.getCenterX() - contentWidth() / 2, bounds.getY() + 4);
		var recipe = display.recipe().value();
		var widgets = Lists.<Widget>newArrayList(Widgets.createRecipeBase(bounds));
		createInfoBadgeIfNeeded(widgets, display, startPoint);
		var xCenter = bounds.getCenterX();
		var y = recipe.getIngredients().size() > 9 || recipe.conditions().showingCount() > 9 ? 26 : 28;
		ingredientGroup(widgets, startPoint, recipe, xCenter - 45 - startPoint.x, y);
		actionGroup(widgets, startPoint, recipe, xCenter + 50 - startPoint.x, y);
		drawExtra(widgets, display, bounds);
		return widgets;
	}

	public void drawExtra(List<Widget> widgets, LycheeDisplay<T> display, Rectangle bounds) {
		var iconBounds = new Rectangle(bounds.getCenterX() - 8, bounds.y + 19, 24, 24);
		widgets.add(Widgets.createDrawableWidget((GuiGraphics graphics, int mouseX, int mouseY, float delta) -> {
			graphics.pose().pushPose();
			graphics.pose().translate(0, 0, 100);
			getIcon().render(graphics, iconBounds, mouseX, mouseY, delta);
			graphics.pose().popPose();
		}));
	}
}
