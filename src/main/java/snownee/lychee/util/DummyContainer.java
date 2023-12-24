package snownee.lychee.util;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface DummyContainer extends Container {
	@Override
	default void clearContent() {
	}

	@Override
	default int getContainerSize() {
		return 0;
	}

	@Override
	default boolean isEmpty() {
		return true;
	}

	@Override
	default @NotNull ItemStack getItem(int index) {
		return ItemStack.EMPTY;
	}

	@Override
	default @NotNull ItemStack removeItem(int index, int count) {
		return ItemStack.EMPTY;
	}

	@Override
	default @NotNull ItemStack removeItemNoUpdate(int index) {
		return ItemStack.EMPTY;
	}

	@Override
	default void setItem(int index, ItemStack stack) {
	}

	@Override
	default void setChanged() {
	}

	@Override
	default boolean stillValid(Player player) {
		return false;
	}
}
