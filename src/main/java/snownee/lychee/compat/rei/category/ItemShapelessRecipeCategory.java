package snownee.lychee.compat.rei.category;

import java.util.List;

import com.google.common.collect.Lists;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public abstract class ItemShapelessRecipeCategory<T extends ILycheeRecipe<LycheeContext>, D extends LycheeDisplay<T>> extends LycheeCategory<T>,
		LycheeDisplayCategory<D> {

	public ItemShapelessRecipeCategory(LycheeRecipeType<LycheeContext, T> recipeType) {
		super();
		infoRect = new Rect2i(3, 25, 8, 8);
	}

	@Override
	public int getDisplayWidth(D display) {
		return contentWidth();
	}

	@Override
	public int contentWidth() {
		return WIDTH + 20;
	}

	@Override
	public List<Widget> setupDisplay(D display, Rectangle bounds) {
		Point startPoint = new Point(bounds.getCenterX() - contentWidth() / 2, bounds.getY() + 4);
		T recipe = display.recipe();
		var widgets = Lists.<Widget>newArrayList(Widgets.createRecipeBase(bounds));
		drawInfoBadgeIfNeededIfNeeded(widgets, display, startPoint);
		int xCenter = bounds.getCenterX();
		int y = recipe.getIngredients().size() > 9 || recipe.conditions().showingCount() > 9 ? 26 : 28;
		ingredientGroup(widgets, startPoint, recipe, xCenter - 45 - startPoint.x, y);
		actionGroup(widgets, startPoint, recipe, xCenter + 50 - startPoint.x, y);
		drawExtra(widgets, display, bounds);
		return widgets;
	}

	public void drawExtra(List<Widget> widgets, D display, Rectangle bounds) {
		Rectangle iconBounds = new Rectangle(bounds.getCenterX() - 8, bounds.y + 19, 24, 24);
		widgets.add(Widgets.createDrawableWidget((GuiGraphics graphics, int mouseX, int mouseY, float delta) -> {
			graphics.pose().pushPose();
			graphics.pose().translate(0, 0, 100);
			getIcon().render(graphics, iconBounds, mouseX, mouseY, delta);
			graphics.pose().popPose();
		}));
	}
}
