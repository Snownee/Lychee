package snownee.lychee.compat.rei.ingredient;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import snownee.lychee.client.core.post.PostActionRenderer;
import snownee.lychee.core.post.PostAction;

public enum PostActionIngredientRenderer implements EntryRenderer<PostAction> {

	INSTANCE;

	@Override
	public void render(EntryStack<PostAction> entry, PoseStack poseStack, Rectangle bounds, int mx, int my, float delta) {
		if (entry.isEmpty())
			return;
		PostActionRenderer.of(entry.getValue()).render(entry.getValue(), poseStack, bounds.x, bounds.y);
	}

	@Override
	public @Nullable Tooltip getTooltip(EntryStack<PostAction> entry, TooltipContext context) {
		return Tooltip.create(PostActionRenderer.of(entry.getValue()).getTooltips(entry.getValue()));
	}

}
