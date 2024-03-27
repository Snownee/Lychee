package snownee.lychee.util.action;

import java.util.List;

import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public interface PostActionDisplay {

	Component getDisplayName();

	default List<BlockPredicate> getOutputBlocks() {
		return List.of();
	}

	default List<ItemStack> getOutputItems() {
		return List.of();
	}

	default boolean hidden() {
		return preventSync();
	}

	default boolean preventSync() {
		return false;
	}

	String toJsonString();
}
