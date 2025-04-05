package snownee.lychee.compat.recipeviewer.rei.ingredient;

import org.jetbrains.annotations.Nullable;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.action.ActionRenderer;

public enum PostActionIngredientRenderer implements EntryRenderer<PostAction> {

	INSTANCE;

	@Override
	public void render(EntryStack<PostAction> entry, GuiGraphics graphics, Rectangle bounds, int mx, int my, float delta) {
		if (entry.isEmpty()) {
			return;
		}
		ActionRenderer.of(entry.getValue()).internalRender(entry.getValue(), graphics, bounds.x, bounds.y);
	}

	@Override
	public @Nullable Tooltip getTooltip(EntryStack<PostAction> entry, TooltipContext context) {
		return Tooltip.create(ActionRenderer.of(entry.getValue()).getTooltips(entry.getValue(), Minecraft.getInstance().player));
	}

}
