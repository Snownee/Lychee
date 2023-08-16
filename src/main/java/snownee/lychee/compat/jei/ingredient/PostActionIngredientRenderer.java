package snownee.lychee.compat.jei.ingredient;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import snownee.lychee.client.core.post.PostActionRenderer;
import snownee.lychee.core.post.PostAction;

public enum PostActionIngredientRenderer implements IIngredientRenderer<PostAction> {

	INSTANCE;

	@Override
	public List<Component> getTooltip(PostAction action, TooltipFlag flag) {
		return PostActionRenderer.of(action).getTooltips(action);
	}

	@Override
	public void render(PoseStack poseStack, PostAction action) {
		PostActionRenderer.of(action).render(action, poseStack, 0, 0);
	}

}
