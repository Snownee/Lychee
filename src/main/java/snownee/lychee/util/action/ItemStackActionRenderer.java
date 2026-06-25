package snownee.lychee.util.action;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public interface ItemStackActionRenderer<T extends PostAction> extends ItemBasedActionRenderer<T> {

	@Override
	default List<Component> getBaseTooltips(T action, @Nullable Player player) {
		ItemStack itemStack = getItem(action).create();
		if (action.commonProperties().customName() != null) {
			itemStack.set(DataComponents.ITEM_NAME, action.commonProperties().customName());
		}
		return itemStack.getTooltipLines(
				Item.TooltipContext.EMPTY,
				player,
				Minecraft.getInstance().options.advancedItemTooltips
						? TooltipFlag.Default.ADVANCED
						: TooltipFlag.Default.NORMAL
		);
	}
}
