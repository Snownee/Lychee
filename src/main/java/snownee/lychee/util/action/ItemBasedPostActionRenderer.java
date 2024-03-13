package snownee.lychee.util.action;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import snownee.lychee.client.gui.GuiGameElement;

public interface ItemBasedPostActionRenderer<T extends PostAction> extends PostActionRenderer<T> {

	ItemStack getItem(T action);

	@Override
	default void render(T action, GuiGraphics graphics, int x, int y) {
		GuiGameElement.of(getItem(action)).render(graphics, x, y);
	}
}
