package snownee.lychee.compat.rei.category;

import java.util.List;

import com.google.common.collect.Lists;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import snownee.lychee.compat.rei.display.LycheeDisplay;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public class ItemShapelessRecipeCategory<T extends ILycheeRecipe<LycheeContext>> extends LycheeDisplayCategory<LycheeDisplay<T>> implements LycheeCategory<T> {

	private static final Rect2i INFO_RECT = new Rect2i(3, 25, 8, 8);
	private final LycheeRecipeType<LycheeContext, T> recipeType;

	public ItemShapelessRecipeCategory(
			CategoryIdentifier<? extends LycheeDisplay<T>> id,
			Renderer icon,
			LycheeRecipeType<LycheeContext, T> recipeType) {
		super(id, icon);
		this.recipeType = recipeType;
	}

	@Override
	public LycheeRecipeType<?, ? extends T> recipeType() {
		return recipeType;
	}

	@Override
	public Rect2i infoRect() {
		return INFO_RECT;
	}

	@Override
	public int getDisplayWidth(LycheeDisplay<T> display) {
		return contentWidth();
	}

	@Override
	public int contentWidth() {
		return WIDTH + 20;
	}

	@Override
	public List<Widget> setupDisplay(LycheeDisplay<T> display, Rectangle bounds) {
		var startPoint = new Point(bounds.getCenterX() - contentWidth() / 2, bounds.getY() + 4);
		var recipe = display.recipe();
		var widgets = Lists.<Widget>newArrayList(Widgets.createRecipeBase(bounds));
		drawInfoBadgeIfNeeded(widgets, display, startPoint);
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
