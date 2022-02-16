package snownee.lychee.core;

import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class ItemShapelessContext extends LycheeContext {
	public final List<ItemEntity> itemEntities;
	public List<ItemEntity> filteredItems;
	public int[] match;
	public int totalItems;

	protected ItemShapelessContext(Random pRandom, Level level, Map<LootContextParam<?>, Object> pParams, List<ItemEntity> itemEntities) {
		super(pRandom, level, pParams);
		this.itemEntities = itemEntities;
		totalItems = itemEntities.stream().map(ItemEntity::getItem).mapToInt(ItemStack::getCount).sum();
	}

	public static class Builder<C extends ItemShapelessContext> extends LycheeContext.Builder<C> {
		public final List<ItemEntity> itemEntities;

		public Builder(Level level, List<ItemEntity> itemEntities) {
			super(level);
			this.itemEntities = itemEntities;
		}

		@Override
		public C create(LootContextParamSet pParameterSet) {
			return (C) new ItemShapelessContext(random, level, params, itemEntities);
		}

	}

}