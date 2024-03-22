package snownee.lychee.compat.rei.category;

import java.util.List;

import com.google.common.base.Strings;
import com.mojang.blaze3d.vertex.PoseStack;

import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import snownee.lychee.client.gui.AllGuiTextures;
import snownee.lychee.util.ClientProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.recipe.ILycheeRecipe;
import snownee.lychee.util.recipe.LycheeRecipeType;

public interface LycheeCategory<R extends ILycheeRecipe<LycheeContext>> {
	int WIDTH = 150;
	int HEIGHT = 59;

	LycheeRecipeType<?, ? extends R> recipeType();

	default int contentWidth() {
		return 120;
	}

	static void drawInfoBadgeIfNeeded(List<Widget> widgets, ILycheeRecipe<?> recipe, Point startPoint, Rect2i rect) {
		if (!recipe.conditions().conditions().isEmpty() || recipe.comment().map(it -> !Strings.isNullOrEmpty(it)).orElse(false)) {
			widgets.add(Widgets.createDrawableWidget((GuiGraphics graphics, int mouseX, int mouseY, float delta) -> {
				PoseStack matrixStack = graphics.pose();
				matrixStack.pushPose();
				matrixStack.translate(startPoint.x + rect.getX(), startPoint.y + rect.getY(), 0);
				matrixStack.scale(.5F, .5F, .5F);
				AllGuiTextures.INFO.render(graphics, 0, 0);
				matrixStack.popPose();
			}));
			ReactiveWidget reactive = new ReactiveWidget(REICompat.offsetRect(startPoint, rect));
			reactive.setTooltipFunction($ -> JEIREI.getRecipeTooltip(recipe).toArray(new Component[0]));
			reactive.setOnClick((widget, button) -> ClientProxy.postInfoBadgeClickEvent(recipe, button));
			widgets.add(reactive);
		}
	}
}
