package snownee.lychee.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.LycheeLootContextParamSets;
import snownee.lychee.util.input.ItemStackHolderCollection;
import snownee.lychee.util.action.ActionMarker;
import snownee.lychee.util.action.PostAction;
import snownee.lychee.util.DummyContainer;
import snownee.lychee.util.LycheeContext;
import snownee.lychee.util.action.ActionRuntime;
import snownee.lychee.util.action.Job;

public class LycheeRecipeContext implements DummyContainer, LycheeContext {
	private final RandomSource random;
	private final Level level;
	private final Map<LootContextParam<?>, Object> params;


	public ActionRuntime runtime;
	public ItemStackHolderCollection itemHolders = ItemStackHolderCollection.EMPTY;

	@Nullable
	public JsonObject json;

	private final Supplier<LootContext> lootContext = Suppliers.memoize(() -> {
		initBlockEntityParam();
		LootParams.Builder paramsBuilder = new LootParams.Builder(serverLevel());
		//noinspection rawtypes,unchecked
		params().forEach((p, o) -> paramsBuilder.withParameter((LootContextParam) p, o));
		LootContext.Builder builder = new LootContext.Builder(paramsBuilder.create(LycheeLootContextParamSets.ALL));
		return builder.create(Optional.empty());
	});

	public LycheeRecipeContext(RandomSource random, Level level, Map<LootContextParam<?>, Object> params) {
		this.random = random;
		this.level = level;
		this.params = params;
	}

	@Override
	public RandomSource random() {
		return random;
	}

	@Override
	public Level level() {
		return level;
	}

	@Override
	public Map<LootContextParam<?>, Object> params() {
		return params;
	}

	@Override
	public LootContext asLootContext() {
		return lootContext.get();
	}

	@Override
	public int getContainerSize() {
		return itemHolders.size();
	}

	@Override
	public @NotNull ItemStack getItem(int index) {
		return itemHolders.get(index).itemstack();
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		itemHolders.replace(index, stack);
	}

	public void enqueueActions(Collection<PostAction<?>> actions, int times, boolean startNew) {
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
		for (final var job : runtime.jobs) {
			jobs.add(job.action().toJson());
			jobRepeats.add(job.times);
		}
		jsonObject.add("jobs", jobs);
		jsonObject.add("jobRepeats", jobRepeats);
		if (json != null) {
			jsonObject.add("json", json);
		}
		return jsonObject;
	}

	public static LycheeRecipeContext load(JsonObject jsonObject, ActionMarker marker) {
		var builder = new LycheeRecipeContext.Builder<>(marker.self().level());
		builder.withParameter(LootContextParams.ORIGIN, marker.self().position());
		LycheeRecipeContext ctx = builder.create(LycheeLootContextParamSets.ALL);
		ctx.runtime = new ActionRuntime();
		ctx.runtime.doDefault = jsonObject.get("doDefault").getAsBoolean();
		JsonArray jobs = jsonObject.getAsJsonArray("jobs");
		JsonArray jobRepeats = jsonObject.getAsJsonArray("jobRepeats");
		List<Job> jobList = Lists.newArrayList();
		for (int i = 0; i < jobs.size(); i++) {
			var job = new Job(
					PostAction.parse(jobs.get(i).getAsJsonObject()),
					jobRepeats.get(i).getAsInt()
			);
			jobList.add(job);
		}
		ctx.runtime.jobs.addAll(0, jobList);
		ctx.runtime.marker = marker;
		if (jsonObject.has("json")) {
			ctx.json = jsonObject.getAsJsonObject("json");
		}
		return ctx;
	}

	public static class Builder<C extends LycheeRecipeContext> {
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

		protected void beforeCreate(LootContextParamSet pParameterSet) {
			//			Set<LootContextParam<?>> set = Sets.difference(this.params.keySet(), pParameterSet.getAllowed
			//			());
			//			if (false && !set.isEmpty()) { // Forge: Allow mods to pass custom loot parameters (not part
			//			of the
			// vanilla loot table) to the loot context.
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
			return (C) new LycheeRecipeContext(random, level, params);
		}

		public void setParams(Map<LootContextParam<?>, Object> params) {
			this.params = params;
		}
	}
}
