package snownee.lychee.context;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.LycheeLootContextParamSets;

public class LootParamsContext {
	private final Map<LootContextParam<?>, @Nullable Object> params = new IdentityHashMap<>();
	private final Level level;
	private final LootContextParamSet paramSet;
	private boolean validated;

	public LootParamsContext(Level level, LootContextParamSet paramSet) {
		this.level = level;
		this.paramSet = paramSet;
	}

	public Map<LootContextParam<?>, @Nullable Object> params() {
		return params;
	}

	public LootContextParamSet paramSet() {
		return paramSet;
	}

	/**
	 * @param param The parameter to check
	 * @return Check whether the given parameter is present in this context.
	 */
	public boolean has(LootContextParam<?> param) {
		return params.get(param) != null;
	}

	/**
	 * @return The value of the given parameter.
	 * @throws NoSuchElementException if the parameter is not present in this context
	 */
	public <T> T get(LootContextParam<T> param) {
		final var result = getOrNull(param);
		if (result == null) {
			throw new NoSuchElementException(param.getName().toString());
		} else {
			return result;
		}
	}

	/**
	 * @return The value of the given parameter if it is present in this context, null otherwise.
	 */
	public @Nullable <T> T getOrNull(LootContextParam<T> param) {
		//noinspection unchecked
		return (T) params.computeIfAbsent(param, this::init);
	}

	public <T> void set(LootContextParam<T> param, @Nullable T value) {
		params.put(param, value);
		if (validated && param == LootContextParams.ORIGIN) {
			for (LootContextParam<?> initParam : LootParamInit.LOOKUP.keySet()) {
				if (initParam == LootContextParams.ORIGIN) {
					continue;
				}
				params.remove(initParam);
			}
		}
	}

	public void remove(LootContextParam<?> param) {
		set(param, null);
	}

	public LootContext asLootContext() {
		initAll();
		var paramsBuilder = new LootParams.Builder((ServerLevel) level);
		params.forEach((p, o) -> {
			if (o != null) {
				//noinspection rawtypes,unchecked
				paramsBuilder.withParameter((LootContextParam) p, o);
			}
		});
		var builder = new LootContext.Builder(paramsBuilder.create(LycheeLootContextParamSets.ALL));
		return builder.create(Optional.empty());
	}

	public void validate() {
		validate(paramSet);
	}

	public void validate(LootContextParamSet paramSet) {
		final var difference = Sets.difference(paramSet.getRequired(), params.keySet());
		if (!difference.isEmpty()) {
			throw new IllegalArgumentException("Missing required parameters: " + difference);
		}
		validated = true;
	}

	public void initAll() {
		for (LootContextParam<?> param : LootParamInit.LOOKUP.keySet()) {
			getOrNull(param);
		}
	}

	@Nullable
	@CheckReturnValue
	public Object init(LootContextParam<?> param) {
		LootParamInit init = LootParamInit.LOOKUP.get(param);
		if (init != null) {
			return init.init(level, this);
		}
		return null;
	}
}