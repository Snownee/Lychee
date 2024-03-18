package snownee.lychee.util.input;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.Lists;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextKey;

public abstract class ItemStackHolderCollection extends ArrayList<ExtendedItemStackHolder> {

	public static final ItemStackHolderCollection EMPTY = InWorld.of();

	public final List<ItemStack> stacksNeedHandle = Lists.newArrayList();

	protected ItemStackHolderCollection(ItemStackHolder... holders) {
		for (final var holder : holders) {
			add(new ExtendedItemStackHolder(holder));
		}
	}

	public ItemStackHolder split(int index, int amount) {
		final var holder = get(index);
		final var original = holder.get();
		var split = holder.split(amount);
		if (!original.isEmpty()) {
			stacksNeedHandle.add(original);
		}
		holder.set(split);
		return holder;
	}

	public ItemStackHolder replace(int index, ItemStack item) {
		final var holder = get(index);
		final var original = holder.replace(item);
		if (!original.isEmpty()) {
			stacksNeedHandle.add(original);
		}
		set(index, holder);
		return holder;
	}

	protected int consumeInputs(int times) {
		var result = 0;

		for (final var holder : this) {
			if (holder.getIgnoreConsumption() || holder.get().isEmpty()) {
				continue;
			}
			var stack = holder.split(times);
			result += stack.getCount();
		}

		return result;
	}

	public abstract int postApply(boolean consumeInputs, int times);

	public static class InWorld extends ItemStackHolderCollection {

		private ItemEntity itemEntity;

		public InWorld(ItemStackHolder.Entity... holders) {
			super(holders);
			if (holders.length > 0) {
				itemEntity = holders[0].getEntity();
			}
		}

		public static ItemStackHolderCollection of(ItemEntity... entities) {
			return new InWorld(Stream.of(entities)
					.map(ItemStackHolder.Entity::new)
					.toArray(ItemStackHolder.Entity[]::new));
		}

		@Override
		public int postApply(boolean consumeInputs, int times) {
			for (ItemStack stack : stacksNeedHandle) {
				if (stack.isEmpty()) {
					continue;
				}
				final var pos = itemEntity.position();
				final var newEntity = new ItemEntity(itemEntity.level(), pos.x, pos.y, pos.z, stack);
				itemEntity.level().addFreshEntity(newEntity);
			}
			return consumeInputs ? consumeInputs(times) : 0;
		}

	}

	public static class Inventory extends ItemStackHolderCollection {

		private final LycheeContext context;

		public Inventory(LycheeContext context, ItemStackHolder.Direct... holders) {
			super(holders);
			this.context = context;
		}

		public static ItemStackHolderCollection of(LycheeContext ctx, ItemStack... items) {
			return new Inventory(
					ctx,
					Stream.of(items).map(ItemStackHolder.Direct::new).toArray(ItemStackHolder.Direct[]::new)
			);
		}

		@Override
		public int postApply(boolean consumeInputs, int times) {
			final var lootParamsContext = context.get(LycheeContextKey.LOOT_PARAMS);
			final var level = context.get(LycheeContextKey.LEVEL);
			final var entity = lootParamsContext.getOrNull(LootContextParams.THIS_ENTITY);
			Player player = null;
			if (entity instanceof Player playerEntity) {
				player = playerEntity;
			}
			final var pos = lootParamsContext.getOrNull(LootContextParams.ORIGIN);

			for (ItemStack stack : stacksNeedHandle) {
				if (player != null) {
					if (!player.addItem(stack)) {
						player.drop(stack, false);
					}
				} else if (pos != null) {
					CommonProxy.dropItemStack(level, pos.x, pos.y, pos.z, stack, null);
				}
			}
			return consumeInputs ? consumeInputs(times) : 0;
		}

	}

}
