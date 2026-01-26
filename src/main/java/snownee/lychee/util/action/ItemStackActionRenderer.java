package snownee.lychee.util.action;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;

public interface ItemStackActionRenderer<T extends PostAction> extends ItemBasedActionRenderer<T> {

	@Override
	default List<Component> getBaseTooltips(T action, @Nullable Player player) {
		return getItem(action).create().getTooltipLines(
				Item.TooltipContext.EMPTY,
				player,
				Minecraft.getInstance().options.advancedItemTooltips
						? TooltipFlag.Default.ADVANCED
						: TooltipFlag.Default.NORMAL
		);
	}
}
