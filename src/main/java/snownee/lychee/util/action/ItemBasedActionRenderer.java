package snownee.lychee.util.action;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStackTemplate;
import snownee.lychee.client.gui.GuiGameElement;

public interface ItemBasedActionRenderer<T extends PostAction> extends ActionRenderer<T> {

	@Nullable
	ItemStackTemplate getItem(T action);

	@Override
	default void render(T action, GuiGraphicsExtractor graphics, int x, int y) {
		ItemStackTemplate item = getItem(action);
		if (item != null) {
			GuiGameElement.of(item).render(graphics, x, y);
		}
	}
}
