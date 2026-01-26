package snownee.lychee.util;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public class Displays {
	public static SlotDisplay slot(@Nullable ItemStackTemplate template) {
		return template == null ? emptySlot() : new SlotDisplay.ItemStackSlotDisplay(template);
	}

	public static SlotDisplay slot(TagKey<Item> tagKey) {
		return new SlotDisplay.TagSlotDisplay(tagKey);
	}

	public static SlotDisplay slot(List<ItemStackTemplate> items) {
		return new SlotDisplay.Composite(items.stream().map(Displays::slot).toList());
	}

	public static SlotDisplay emptySlot() {
		return SlotDisplay.Empty.INSTANCE;
	}
}
