package snownee.lychee.util.input;

import net.minecraft.world.item.ItemStack;

/**
 * {@link ItemStackHolder} that has extra fields
 */
public class ExtendedItemStackHolder implements ItemStackHolder {
	private final ItemStackHolder holder;

	private boolean ignoreConsumption;

	public ExtendedItemStackHolder(ItemStackHolder holder) {this.holder = holder;}

	@Override
	public void set(final ItemStack stack) {holder.set(stack);}

	@Override
	public ItemStack replace(final ItemStack item) {return holder.replace(item);}

	@Override
	public ItemStack split(final int amount) {
		return holder.split(amount);
	}

	@Override
	public ItemStack get() {return holder.get();}

	public ItemStackHolder holder() {
		return holder;
	}

	public boolean getIgnoreConsumption() {return ignoreConsumption;}

	public void setIgnoreConsumption(final boolean ignoreConsumption) {this.ignoreConsumption = ignoreConsumption;}
}
