package snownee.lychee.util.action;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import snownee.lychee.util.recipe.ILycheeRecipe;

public interface PostActionDisplay {

	Component getDisplayName();

	default List<BlockPredicate> getOutputBlocks() {
		return List.of();
	}

	default List<SlotDisplay> getOutputItems() {
		return List.of();
	}

	default boolean hidden() {
		return preventSync();
	}

	default boolean preventSync() {
		return false;
	}

	String toJsonString();

	default SlotDisplay transformRemainder(SlotDisplay itemStack, @Nullable ILycheeRecipe<?> recipe) {
		return SlotDisplay.Empty.INSTANCE;
	}
}
