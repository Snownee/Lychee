package snownee.lychee.core.input;

import java.util.BitSet;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public abstract class ItemHolderCollection {

	public static final ItemHolderCollection EMPTY = InWorld.of();

	protected final ItemHolder[] holders;
	protected final List<ItemStack> tempList = Lists.newArrayList();
	public final BitSet ignoreConsumptionFlags;

	public ItemHolderCollection(ItemHolder... holders) {
		this.holders = holders;
		ignoreConsumptionFlags = new BitSet(holders.length);
	}

	public ItemHolder get(int index) {
		return holders[index];
	}

	public ItemHolder split(int index, int amount) {
		ItemHolder holder = get(index).split(amount, tempList::add);
		holders[index] = holder;
		return holder;
	}

	public ItemHolder replace(int index, ItemStack item) {
		ItemHolder holder = get(index).replace(item, tempList::add);
		holders[index] = holder;
		return holder;
	}

	protected int consumeInputs(int times) {
		int total = 0;
		for (int i = 0; i < holders.length; i++) {
			ItemHolder holder = holders[i];
			if (!ignoreConsumptionFlags.get(i) && !holder.get().isEmpty()) {
				holders[i].get().shrink(times);
				total += times;
			}
		}
		return total;
	}

	public abstract int postApply(boolean consumeInputs, int times);

	public int size() {
		return holders.length;
	}

	public static class InWorld extends ItemHolderCollection {

		private ItemEntity itemEntity;

		public InWorld(ItemHolder.InWorld... holders) {
			super(holders);
			if (holders.length > 0) {
				itemEntity = holders[0].getEntity();
			}
		}

		public static ItemHolderCollection of(ItemEntity... entities) {
			return new InWorld(Stream.of(entities).map(ItemHolder.InWorld::new).toArray(ItemHolder.InWorld[]::new));
		}

		@Override
		public int postApply(boolean consumeInputs, int times) {
			for (ItemStack stack : tempList) {
				if (stack.isEmpty()) {
					continue;
				}
				Vec3 pos = itemEntity.position();
				ItemEntity newEntity = new ItemEntity(itemEntity.level, pos.x, pos.y, pos.z, stack);
				itemEntity.level.addFreshEntity(newEntity);
			}
			return consumeInputs ? consumeInputs(times) : 0;
		}

	}

	public static class Inventory extends ItemHolderCollection {

		private Player player;

		public Inventory(Player player, ItemHolder.Simple... holders) {
			super(holders);
			this.player = player;
		}

		public static ItemHolderCollection of(Player player, ItemStack... items) {
			return new Inventory(player, Stream.of(items).map(ItemHolder.Simple::new).toArray(ItemHolder.Simple[]::new));
		}

		@Override
		public int postApply(boolean consumeInputs, int times) {
			for (ItemStack stack : tempList) {
				if (!player.addItem(stack)) {
					player.drop(stack, false);
				}
			}
			return consumeInputs ? consumeInputs(times) : 0;
		}

	}

}
