package snownee.lychee.compat.recipeviewer.jei.ingredient;

import java.util.List;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import snownee.lychee.action.PlaceBlock;
import snownee.lychee.util.action.ActionRenderer;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.predicates.BlockPredicateExtensions;


public enum PostActionIngredientRenderer implements IIngredientRenderer<PostAction> {

	INSTANCE;

	public static final PostAction INGREDIENT_HACK_DUMMY = new PlaceBlock(
			PostActionCommonProperties.EMPTY,
			BlockPredicateExtensions.ANY,
			BlockPos.ZERO);

	@SuppressWarnings("removal")
	@Override
	public List<Component> getTooltip(PostAction ingredient, TooltipFlag tooltipFlag) {
		return List.of();
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, PostAction ingredient, TooltipFlag tooltipFlag) {
		if (ingredient == INGREDIENT_HACK_DUMMY) {
			return;
		}
		var player = Minecraft.getInstance().player;
		tooltip.addAll(ActionRenderer.of(ingredient).getTooltips(ingredient, player));
	}

	@Override
	public void render(GuiGraphics graphics, PostAction action) {
		if (action == INGREDIENT_HACK_DUMMY) {
			return;
		}
		ActionRenderer.of(action).internalRender(action, graphics, 0, 0);
	}
}