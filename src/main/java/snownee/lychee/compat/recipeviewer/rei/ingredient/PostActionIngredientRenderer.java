package snownee.lychee.compat.recipeviewer.rei.ingredient;

import org.jspecify.annotations.Nullable;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.compat.GuiGraphics;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import snownee.lychee.action.PlaceBlock;
import snownee.lychee.util.action.ActionRenderer;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.PostActionCommonProperties;
import snownee.lychee.util.predicates.BlockPredicateExtensions;

public enum PostActionIngredientRenderer implements EntryRenderer<PostAction> {
	INSTANCE;

	public static final PostAction INGREDIENT_HACK_DUMMY = new PlaceBlock(
			PostActionCommonProperties.EMPTY,
			BlockPredicateExtensions.ANY,
			BlockPos.ZERO,
			false);

	@Override
	public void render(EntryStack<PostAction> entry, GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
		PostAction action = entry.getValue();
		if (entry.isEmpty() || action == INGREDIENT_HACK_DUMMY) {
			return;
		}
		ActionRenderer.of(action).internalRender(action, graphics, bounds.x, bounds.y);
	}

	@Override
	public @Nullable Tooltip getTooltip(EntryStack<PostAction> entry, TooltipContext context) {
		PostAction action = entry.getValue();
		if (action == INGREDIENT_HACK_DUMMY) {
			return null;
		}
		return Tooltip.create(ActionRenderer.of(action).getTooltips(action, Minecraft.getInstance().player));
	}
}
