package snownee.lychee.block_crushing;

import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.lychee.core.LycheeContext;

public class ItemShapelessContext extends LycheeContext {
	public final List<ItemEntity> itemEntities;
	public final FallingBlockEntity fallingBlock;
	public int[] match;
	public int totalItems;

	protected ItemShapelessContext(Random pRandom, Level level, Map<LootContextParam<?>, Object> pParams, List<ItemEntity> itemEntities, FallingBlockEntity fallingBlock) {
		super(pRandom, level, pParams);
		this.itemEntities = itemEntities;
		this.fallingBlock = fallingBlock;
		totalItems = itemEntities.stream().map(ItemEntity::getItem).mapToInt(ItemStack::getCount).sum();
	}

	public static class Builder extends LycheeContext.Builder<ItemShapelessContext> {
		public final List<ItemEntity> itemEntities;
		public final FallingBlockEntity fallingBlock;

		public Builder(Level level, List<ItemEntity> itemEntities, FallingBlockEntity fallingBlock) {
			super(level);
			this.itemEntities = itemEntities;
			this.fallingBlock = fallingBlock;
		}

		@Override
		public ItemShapelessContext create(LootContextParamSet pParameterSet) {
			return new ItemShapelessContext(random, level, params, itemEntities, fallingBlock);
		}

	}

}
