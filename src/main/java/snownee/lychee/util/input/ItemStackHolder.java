package snownee.lychee.util.input;

import java.util.function.Supplier;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public interface ItemStackHolder extends Supplier<ItemStack> {
	void set(ItemStack stack);

	/**
	 * @param item The item to replace the original item. Will shrink the original item.
	 * @return The original {@link ItemStack} that is shrunk.
	 */
	default ItemStack replace(ItemStack item) {
		final var original = get();
		if (!original.isEmpty()) {
			original.shrink(item.getCount());
		}
		set(item);
		return original;
	}

	default ItemStack split(int amount) {
		return get().split(amount);
	}

	class Direct implements ItemStackHolder {
		private ItemStack item;

		public Direct(ItemStack item) {
			this.item = item;
		}

		@Override
		public ItemStack get() {
			return item;
		}

		@Override
		public void set(ItemStack stack) {
			item = stack;
		}
	}

	class Entity implements ItemStackHolder {
		private final ItemEntity itemEntity;

		public Entity(ItemEntity itemEntity) {
			this.itemEntity = itemEntity;
		}

		@Override
		public ItemStack get() {
			return itemEntity.getItem();
		}

		@Override
		public void set(ItemStack stack) {
			itemEntity.setItem(stack);
		}

		public ItemEntity getEntity() {
			return itemEntity;
		}
	}
}
