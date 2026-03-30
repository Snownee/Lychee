package snownee.lychee.util.action;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStackTemplate;
import snownee.lychee.client.gui.GuiGameElement;

public interface ItemBasedActionRenderer<T extends PostAction> extends ActionRenderer<T> {

	ItemStackTemplate getItem(T action);

	@Override
	default void render(T action, GuiGraphicsExtractor graphics, int x, int y) {
		GuiGameElement.of(getItem(action)).render(graphics, x, y);
	}
}
