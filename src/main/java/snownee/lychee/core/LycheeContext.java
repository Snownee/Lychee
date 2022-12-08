package snownee.lychee.core;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.LycheeLootContextParamSets;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.core.input.ItemHolderCollection;
import snownee.lychee.core.post.Delay.LycheeMarker;
import snownee.lychee.core.post.PostAction;

public class LycheeContext extends EmptyContainer {
	private final RandomSource random;
	private final Map<LootContextParam<?>, Object> params;
	private final Level level;
	private LootContext cachedLootContext;
	public ActionRuntime runtime;
	public ItemHolderCollection itemHolders = ItemHolderCollection.EMPTY;

	protected LycheeContext(RandomSource pRandom, Level level, Map<LootContextParam<?>, Object> pParams) {
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

	public RandomSource getRandom() {
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
			cachedLootContext = builder.create(LycheeLootContextParamSets.ALL);
		}
		return cachedLootContext;
	}

	public void lazyGetBlockEntity() {
		if (params.containsKey(LootContextParams.BLOCK_ENTITY)) {
			return;
		}
		BlockPos pos = getParamOrNull(LycheeLootContextParams.BLOCK_POS);
		if (pos == null) {
			pos = new BlockPos(getParam(LootContextParams.ORIGIN));
			setParam(LycheeLootContextParams.BLOCK_POS, pos);
		}
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity != null)
			setParam(LootContextParams.BLOCK_ENTITY, blockEntity);
	}

	public void setParam(LootContextParam<?> param, Object value) {
		params.put(param, value);
	}

	public void removeParam(LootContextParam<?> param) {
		params.remove(param);
	}

	@Override
	public int getContainerSize() {
		return itemHolders.size();
	}

	@Override
	public ItemStack getItem(int index) {
		return itemHolders.get(index).get();
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		itemHolders.replace(index, stack);
	}

	public void enqueueActions(List<PostAction> actions, int times, boolean startNew) {
		if (runtime == null || startNew) {
			runtime = new ActionRuntime();
		}
		runtime.enqueue(actions, times);
	}

	public JsonObject save() {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("doDefault", runtime.doDefault);
		JsonArray jobs = new JsonArray(runtime.jobs.size());
		JsonArray jobRepeats = new JsonArray(runtime.jobs.size());
		for (var job : runtime.jobs) {
			jobs.add(job.action.toJson());
			jobRepeats.add(job.times);
		}
		jsonObject.add("jobs", jobs);
		jsonObject.add("jobRepeats", jobRepeats);
		return jsonObject;
	}

	public static LycheeContext load(JsonObject jsonObject, LycheeMarker marker) {
		var builder = new LycheeContext.Builder<>(marker.getEntity().level);
		builder.withParameter(LootContextParams.ORIGIN, marker.getEntity().position());
		LycheeContext ctx = builder.create(LycheeLootContextParamSets.ALL);
		ctx.runtime.doDefault = jsonObject.get("doDefault").getAsBoolean();
		JsonArray jobs = jsonObject.getAsJsonArray("jobs");
		JsonArray jobRepeats = jsonObject.getAsJsonArray("jobRepeats");
		List<Job> jobList = Lists.newArrayList();
		for (int i = 0; i < jobs.size(); i++) {
			var job = new Job(PostAction.parse(jobs.get(i).getAsJsonObject()), jobRepeats.get(i).getAsInt());
			jobList.add(job);
		}
		ctx.runtime.jobs.addAll(0, jobList);
		ctx.runtime.marker = marker;
		return ctx;
	}

	public static class Builder<C extends LycheeContext> {
		protected Map<LootContextParam<?>, Object> params = Maps.newIdentityHashMap();
		protected Level level;
		protected RandomSource random;

		public Builder(Level level) {
			this.level = level;
		}

		public Builder<C> withRandom(RandomSource pRandom) {
			random = pRandom;
			return this;
		}

		public Builder<C> withOptionalRandomSeed(long pSeed) {
			if (pSeed != 0L) {
				random = RandomSource.create(pSeed);
			}

			return this;
		}

		public Builder<C> withOptionalRandomSeed(long pSeed, RandomSource pRandom) {
			if (pSeed == 0L) {
				random = pRandom;
			} else {
				random = RandomSource.create(pSeed);
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
				this.random = RandomSource.create();
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
