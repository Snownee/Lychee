package snownee.lychee.core;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.lychee.core.input.ItemHolderCollection;
import snownee.lychee.util.RecipeMatcher;

public class ItemShapelessContext extends LycheeContext {
	public final List<ItemEntity> itemEntities;
	public List<ItemEntity> filteredItems;
	private RecipeMatcher<ItemStack> match;
	public int totalItems;

	protected ItemShapelessContext(RandomSource pRandom, Level level, Map<LootContextParam<?>, Object> pParams, List<ItemEntity> itemEntities) {
		super(pRandom, level, pParams);
		this.itemEntities = itemEntities;
		totalItems = itemEntities.stream().map(ItemEntity::getItem).mapToInt(ItemStack::getCount).sum();
	}

	public void setMatch(@Nullable RecipeMatcher<ItemStack> match) {
		this.match = match;
		if (match == null) {
			itemHolders = ItemHolderCollection.EMPTY;
			return;
		}
		ItemEntity[] entities = new ItemEntity[match.tests.size()];
		for (int i = 0; i < match.inputUsed.length; i++) {
			for (int j = 0; j < match.inputUsed[i]; j++) {
				entities[match.use[i][j]] = filteredItems.get(i);
			}
		}
		itemHolders = ItemHolderCollection.InWorld.of(entities);
	}

	@Nullable
	public RecipeMatcher<ItemStack> getMatch() {
		return match;
	}

	public static class Builder<C extends ItemShapelessContext> extends LycheeContext.Builder<C> {
		public final List<ItemEntity> itemEntities;

		public Builder(Level level, List<ItemEntity> itemEntities) {
			super(level);
			this.itemEntities = itemEntities;
		}

		@Override
		public C create(LootContextParamSet pParameterSet) {
			beforeCreate(pParameterSet);
			return (C) new ItemShapelessContext(random, level, params, itemEntities);
		}
	}

}
