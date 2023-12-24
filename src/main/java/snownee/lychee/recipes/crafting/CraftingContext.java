package snownee.lychee.recipes.crafting;

import java.util.Map;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import snownee.lychee.core.LycheeRecipeContext;

public class CraftingContext extends LycheeRecipeContext {

	public final int matchX;
	public final int matchY;
	public final boolean mirror;

	private CraftingContext(
			RandomSource pRandom,
			Level level,
			Map<LootContextParam<?>, Object> pParams,
			int matchX,
			int matchY,
			boolean mirror
	) {
		super(pRandom, level, pParams);
		this.matchX = matchX;
		this.matchY = matchY;
		this.mirror = mirror;
	}

	public static class Builder extends LycheeRecipeContext.Builder<CraftingContext> {

		private final int matchX;
		private final int matchY;
		private final boolean mirror;

		public Builder(Level level, int matchX, int matchY, boolean mirror) {
			super(level);
			this.matchX = matchX;
			this.matchY = matchY;
			this.mirror = mirror;
		}

		@Override
		public CraftingContext create(LootContextParamSet pParameterSet) {
			beforeCreate(pParameterSet);
			return new CraftingContext(random, level, params, matchX, matchY, mirror);
		}

	}

}
