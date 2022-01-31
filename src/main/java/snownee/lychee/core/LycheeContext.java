package snownee.lychee.core;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class LycheeContext extends EmptyContainer {
	private final Random random;
	private final Map<LootContextParam<?>, Object> params;
	private final Level level;
	private LootContext cachedLootContext;

	protected LycheeContext(Random pRandom, Level level, Map<LootContextParam<?>, Object> pParams) {
		this.random = pRandom;
		this.level = level;
		this.params = ImmutableMap.copyOf(pParams);
	}

	/**
	    * Check whether the given parameter is present in this context.
	    */
	public boolean hasParam(LootContextParam<?> pParameter) {
		return this.params.containsKey(pParameter);
	}

	/**
	    * Get the value of the given parameter.
	    * 
	    * @throws NoSuchElementException if the parameter is not present in this context
	    */
	public <T> T getParam(LootContextParam<T> pParam) {
		T t = (T) this.params.get(pParam);
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
		return (T) this.params.get(pParameter);
	}

	public Random getRandom() {
		return this.random;
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
			LootContext.Builder builder = new LootContext.Builder((ServerLevel) level);
			builder.withRandom(random);
			params.forEach((p, o) -> builder.withParameter((LootContextParam) p, o));
			cachedLootContext = builder.create(LootContextParamSets.EMPTY);
		}
		return cachedLootContext;
	}

	public static class Builder {
		protected final Map<LootContextParam<?>, Object> params = Maps.newIdentityHashMap();
		protected Level level;
		protected Random random;

		public Builder(Level level) {
			this.level = level;
		}

		public Builder withRandom(Random pRandom) {
			this.random = pRandom;
			return this;
		}

		public Builder withOptionalRandomSeed(long pSeed) {
			if (pSeed != 0L) {
				this.random = new Random(pSeed);
			}

			return this;
		}

		public Builder withOptionalRandomSeed(long pSeed, Random pRandom) {
			if (pSeed == 0L) {
				this.random = pRandom;
			} else {
				this.random = new Random(pSeed);
			}

			return this;
		}

		public <T> Builder withParameter(LootContextParam<T> pParameter, T pValue) {
			this.params.put(pParameter, pValue);
			return this;
		}

		public <T> Builder withOptionalParameter(LootContextParam<T> pParameter, @Nullable T pValue) {
			if (pValue == null) {
				this.params.remove(pParameter);
			} else {
				this.params.put(pParameter, pValue);
			}

			return this;
		}

		public <T> T getParameter(LootContextParam<T> pParameter) {
			T t = (T) this.params.get(pParameter);
			if (t == null) {
				throw new IllegalArgumentException("No parameter " + pParameter);
			} else {
				return t;
			}
		}

		@Nullable
		public <T> T getOptionalParameter(LootContextParam<T> pParameter) {
			return (T) this.params.get(pParameter);
		}

		public LycheeContext create(LootContextParamSet pParameterSet) {
			//			Set<LootContextParam<?>> set = Sets.difference(this.params.keySet(), pParameterSet.getAllowed());
			//			if (false && !set.isEmpty()) { // Forge: Allow mods to pass custom loot parameters (not part of the vanilla loot table) to the loot context.
			//				throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + set);
			//			} else {
			Set<LootContextParam<?>> set1 = Sets.difference(pParameterSet.getRequired(), this.params.keySet());
			if (!set1.isEmpty()) {
				throw new IllegalArgumentException("Missing required parameters: " + set1);
			} else {
				Random random = this.random;
				if (random == null) {
					random = new Random();
				}

				return new LycheeContext(random, level, this.params);
			}
			//			}
		}
	}

}
