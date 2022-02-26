package snownee.lychee.core;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import snownee.lychee.LycheeLootContextParams;

public class LycheeContext extends EmptyContainer {
	private final Random random;
	private final Map<LootContextParam<?>, Object> params;
	private final Level level;
	private LootContext cachedLootContext;

	protected LycheeContext(Random pRandom, Level level, Map<LootContextParam<?>, Object> pParams) {
		random = pRandom;
		this.level = level;
		params = pParams;
	}

	/**
	    * Check whether the given parameter is present in this context.
	    */
	public boolean hasParam(LootContextParam<?> pParameter) {
		if (pParameter == LootContextParams.BLOCK_ENTITY)
			lazyGetBlockEntity();
		return params.containsKey(pParameter);
	}

	/**
	    * Get the value of the given parameter.
	    *
	    * @throws NoSuchElementException if the parameter is not present in this context
	    */
	public <T> T getParam(LootContextParam<T> pParam) {
		if (pParam == LootContextParams.BLOCK_ENTITY)
			lazyGetBlockEntity();
		T t = (T) params.get(pParam);
		if (t == null) {
			throw new NoSuchElementException(pParam.getName().toString());
		} else {
			return t;
		}
	}

	/**
	    * Get the value of the given parameter if it is present in this context, null otherwise.
	    */
	@Nullable
	public <T> T getParamOrNull(LootContextParam<T> pParameter) {
		if (pParameter == LootContextParams.BLOCK_ENTITY)
			lazyGetBlockEntity();
		return (T) params.get(pParameter);
	}

	public Random getRandom() {
		return random;
	}

	public Level getLevel() {
		return level;
	}

	public ServerLevel getServerLevel() {
		return (ServerLevel) level;
	}

	@SuppressWarnings("rawtypes")
	public LootContext toLootContext() {
		if (cachedLootContext == null) {
			lazyGetBlockEntity();
			LootContext.Builder builder = new LootContext.Builder((ServerLevel) level);
			builder.withRandom(random);
			params.forEach((p, o) -> builder.withParameter((LootContextParam) p, o));
			if (params.containsKey(LycheeLootContextParams.BLOCK_POS)) {
				builder.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(getParam(LycheeLootContextParams.BLOCK_POS)));
			}
			cachedLootContext = builder.create(LootContextParamSets.EMPTY);
		}
		return cachedLootContext;
	}

	public void lazyGetBlockEntity() {
		if (params.containsKey(LootContextParams.BLOCK_ENTITY)) {
			return;
		}
		BlockPos pos = getParamOrNull(LycheeLootContextParams.BLOCK_POS);
		if (pos == null) {
			Vec3 vec = getParamOrNull(LootContextParams.ORIGIN);
			if (vec == null) {
				return;
			}
			pos = new BlockPos(vec);
		}
		BlockEntity blockEntity = level.getBlockEntity(pos);
		params.put(LootContextParams.BLOCK_ENTITY, blockEntity);
	}

	public static class Builder<C extends LycheeContext> {
		protected Map<LootContextParam<?>, Object> params = Maps.newIdentityHashMap();
		protected Level level;
		protected Random random;

		public Builder(Level level) {
			this.level = level;
		}

		public Builder<C> withRandom(Random pRandom) {
			random = pRandom;
			return this;
		}

		public Builder<C> withOptionalRandomSeed(long pSeed) {
			if (pSeed != 0L) {
				random = new Random(pSeed);
			}

			return this;
		}

		public Builder<C> withOptionalRandomSeed(long pSeed, Random pRandom) {
			if (pSeed == 0L) {
				random = pRandom;
			} else {
				random = new Random(pSeed);
			}

			return this;
		}

		public <T> Builder<C> withParameter(LootContextParam<T> pParameter, T pValue) {
			params.put(pParameter, pValue);
			return this;
		}

		public <T> Builder<C> withOptionalParameter(LootContextParam<T> pParameter, @Nullable T pValue) {
			if (pValue == null) {
				params.remove(pParameter);
			} else {
				params.put(pParameter, pValue);
			}

			return this;
		}

		public <T> T getParameter(LootContextParam<T> pParameter) {
			T t = (T) params.get(pParameter);
			if (t == null) {
				throw new IllegalArgumentException("No parameter " + pParameter);
			} else {
				return t;
			}
		}

		@Nullable
		public <T> T getOptionalParameter(LootContextParam<T> pParameter) {
			return (T) params.get(pParameter);
		}

		protected void beforeCreate(LootContextParamSet pParameterSet) {
			//			Set<LootContextParam<?>> set = Sets.difference(this.params.keySet(), pParameterSet.getAllowed());
			//			if (false && !set.isEmpty()) { // Forge: Allow mods to pass custom loot parameters (not part of the vanilla loot table) to the loot context.
			//				throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + set);
			//			} else {
			Set<LootContextParam<?>> set1 = Sets.difference(pParameterSet.getRequired(), params.keySet());
			if (!set1.isEmpty()) {
				throw new IllegalArgumentException("Missing required parameters: " + set1);
			} else if (this.random == null) {
				this.random = new Random();
			}
			//			}
		}

		public C create(LootContextParamSet pParameterSet) {
			beforeCreate(pParameterSet);
			return (C) new LycheeContext(random, level, params);
		}

		public void setParams(Map<LootContextParam<?>, Object> params) {
			this.params = params;
		}
	}

}
