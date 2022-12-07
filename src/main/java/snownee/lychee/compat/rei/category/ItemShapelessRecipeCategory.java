package snownee.lychee.compat.rei.category;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.Rect2i;
import snownee.lychee.compat.rei.display.BaseREIDisplay;
import snownee.lychee.core.ItemShapelessContext;
import snownee.lychee.core.recipe.LycheeRecipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;

public class ItemShapelessRecipeCategory<C extends ItemShapelessContext, T extends LycheeRecipe<C>, D extends BaseREIDisplay<T>> extends BaseREICategory<C, T, D> {

	public ItemShapelessRecipeCategory(LycheeRecipeType<C, T> recipeType, Renderer icon) {
		super(recipeType);
		this.icon = icon;
		infoRect = new Rect2i(3, 25, 8, 8);
	}

	@Override
	public CategoryIdentifier<? extends D> getCategoryIdentifier() {
		return CategoryIdentifier.of(recipeTypes.get(0).id);
	}

	@Override
	public int getDisplayWidth(D display) {
		return getRealWidth();
	}

	@Override
	public int getRealWidth() {
		return width + 20;
	}

	@Override
	public List<Widget> setupDisplay(D display, Rectangle bounds) {
		Point startPoint = new Point(bounds.getCenterX() - getRealWidth() / 2, bounds.getY() + 4);
		T recipe = display.recipe;
		List<Widget> widgets = super.setupDisplay(display, bounds);
		drawInfoBadge(widgets, display, startPoint);
		int xCenter = bounds.getCenterX();
		int y = recipe.getIngredients().size() > 9 || recipe.getShowingPostActions().size() > 9 ? 26 : 28;
		ingredientGroup(widgets, startPoint, recipe, xCenter - 45 - startPoint.x, y);
		actionGroup(widgets, startPoint, recipe, xCenter + 50 - startPoint.x, y);
		drawExtra(widgets, display, bounds);
		return widgets;
	}

	public void drawExtra(List<Widget> widgets, D display, Rectangle bounds) {
		Rectangle iconBounds = new Rectangle(bounds.getCenterX() - 8, bounds.y + 19, 24, 24);
		widgets.add(Widgets.createDrawableWidget((GuiComponent helper, PoseStack matrixStack, int mouseX, int mouseY, float delta) -> {
			icon.render(matrixStack, iconBounds, mouseX, mouseY, delta);
		}));
	}

}
