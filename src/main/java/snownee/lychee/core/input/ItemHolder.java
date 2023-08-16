package snownee.lychee.core.input;

import java.util.function.Consumer;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public abstract class ItemHolder {

	public abstract ItemStack get();

	protected abstract void set(ItemStack stack);

	public ItemHolder replace(ItemStack item, Consumer<ItemStack> consumer) {
		if (!get().isEmpty()) {
			get().shrink(item.getCount());
			if (!get().isEmpty()) {
				consumer.accept(get());
			}
		}
		set(item);
		return this;
	}

	public ItemHolder split(int amount, Consumer<ItemStack> consumer) {
		ItemStack stack = get().split(amount);
		if (!get().isEmpty()) {
			consumer.accept(get());
		}
		set(stack);
		return this;
	}

	public static class Simple extends ItemHolder {

		private ItemStack item;

		public Simple(ItemStack item) {
			this.item = item;
		}

		@Override
		public ItemStack get() {
			return item;
		}

		@Override
		protected void set(ItemStack stack) {
			item = stack;
		}

	}

	public static class InWorld extends ItemHolder {

		private final ItemEntity itemEntity;

		public InWorld(ItemEntity itemEntity) {
			this.itemEntity = itemEntity;
		}

		@Override
		public ItemStack get() {
			return itemEntity.getItem();
		}

		@Override
		protected void set(ItemStack stack) {
			itemEntity.setItem(stack);
		}

		public ItemEntity getEntity() {
			return itemEntity;
		}

	}

}
