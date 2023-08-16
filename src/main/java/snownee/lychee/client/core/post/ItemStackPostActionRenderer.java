package snownee.lychee.client.core.post;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import snownee.lychee.core.post.PostAction;

public interface ItemStackPostActionRenderer<T extends PostAction> extends ItemBasedPostActionRenderer<T> {

	@Override
	default List<Component> getBaseTooltips(T action) {
		return getItem(action).getTooltipLines(null, Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
	}
}
