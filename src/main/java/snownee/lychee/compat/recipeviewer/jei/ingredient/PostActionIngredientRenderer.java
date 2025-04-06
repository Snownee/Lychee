package snownee.lychee.compat.recipeviewer.jei.ingredient;

import java.util.List;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import snownee.lychee.util.action.ActionRenderer;
import snownee.lychee.util.action.PostAction;


public enum PostActionIngredientRenderer implements IIngredientRenderer<PostAction> {

	INSTANCE;

	@SuppressWarnings("removal")
	@Override
	public List<Component> getTooltip(PostAction ingredient, TooltipFlag tooltipFlag) {
		return List.of();
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, PostAction ingredient, TooltipFlag tooltipFlag) {
		var player = Minecraft.getInstance().player;
		tooltip.addAll(ActionRenderer.of(ingredient).getTooltips(ingredient, player));
	}

	@Override
	public void render(GuiGraphics graphics, PostAction action) {
		ActionRenderer.of(action).internalRender(action, graphics, 0, 0);
	}
}