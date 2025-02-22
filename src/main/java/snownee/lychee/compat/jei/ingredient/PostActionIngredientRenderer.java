package snownee.lychee.compat.jei.ingredient;

import java.util.List;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import snownee.kiwi.util.NotNullByDefault;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionRenderer;

@NotNullByDefault
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
		tooltip.addAll(PostActionRenderer.of(ingredient).getTooltips(ingredient, player));
	}

	@Override
	public void render(GuiGraphics graphics, PostAction action) {
		PostActionRenderer.of(action).render(action, graphics, 0, 0);
	}
}