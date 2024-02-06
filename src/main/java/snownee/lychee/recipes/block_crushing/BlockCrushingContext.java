package snownee.lychee.recipes.block_crushing;

import java.util.List;
import java.util.Map;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.lychee.context.ItemShapelessContext;
import snownee.lychee.core.LycheeRecipeContext;

public class BlockCrushingContext extends ItemShapelessContext {

	public final FallingBlockEntity fallingBlock;

	protected BlockCrushingContext(
			RandomSource pRandom,
			Level level,
			Map<LootContextParam<?>, Object> pParams,
			List<ItemEntity> itemEntities,
			FallingBlockEntity fallingBlock
	) {
		super(pRandom, level, pParams, itemEntities);
		this.fallingBlock = fallingBlock;
	}

	public static class Builder extends LycheeRecipeContext.Builder<BlockCrushingContext> {
		public final List<ItemEntity> itemEntities;
		public final FallingBlockEntity fallingBlock;

		public Builder(Level level, List<ItemEntity> itemEntities, FallingBlockEntity fallingBlock) {
			super(level);
			this.itemEntities = itemEntities;
			this.fallingBlock = fallingBlock;
		}

		@Override
		public BlockCrushingContext create(LootContextParamSet pParameterSet) {
			beforeCreate(pParameterSet);
			return new BlockCrushingContext(random, level, params, itemEntities, fallingBlock);
		}
	}

}
