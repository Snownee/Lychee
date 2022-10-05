package snownee.lychee.dripstone_dripping;

import java.util.Map;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.lychee.core.LycheeContext;

public class DripstoneContext extends LycheeContext {

	public final BlockState source;

	protected DripstoneContext(RandomSource pRandom, Level level, Map<LootContextParam<?>, Object> pParams, BlockState source) {
		super(pRandom, level, pParams);
		this.source = source;
	}

	public static class Builder extends LycheeContext.Builder<DripstoneContext> {
		public final BlockState source;

		public Builder(Level level, BlockState source) {
			super(level);
			this.source = source;
		}

		@Override
		public DripstoneContext create(LootContextParamSet pParameterSet) {
			beforeCreate(pParameterSet);
			return new DripstoneContext(random, level, params, source);
		}
	}
}
