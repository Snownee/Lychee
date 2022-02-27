package snownee.lychee.compat.jei.ingredient;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import snownee.lychee.core.post.PostAction;

public enum PostActionIngredientRenderer implements IIngredientRenderer<PostAction> {

	INSTANCE;

	@Override
	public List<Component> getTooltip(PostAction action, TooltipFlag flag) {
		return action.getTooltips();
	}

	@Override
	public void render(PoseStack poseStack, int mx, int my, @Nullable PostAction action) {
		if (action != null)
			action.render(poseStack, mx, my);
	}

}
