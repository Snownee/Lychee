package snownee.lychee.client.core.post;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.item.ItemStack;
import snownee.lychee.client.gui.GuiGameElement;
import snownee.lychee.core.post.PostAction;

public interface ItemBasedPostActionRenderer<T extends PostAction> extends PostActionRenderer<T> {

	ItemStack getItem(T action);

	@Override
	default void render(T action, PoseStack poseStack, int x, int y) {
		GuiGameElement.of(getItem(action)).render(poseStack, x, y);
	}
}
