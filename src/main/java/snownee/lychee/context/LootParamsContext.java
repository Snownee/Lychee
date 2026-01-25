package snownee.lychee.context;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.jetbrains.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;

import com.google.common.collect.Sets;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.LycheeLootContextParamSets;

public class LootParamsContext {
	private final Map<ContextKey<?>, @Nullable Object> params = new IdentityHashMap<>();
	private final Level level;
	private final ContextKeySet paramSet;
	private boolean validated;

	public LootParamsContext(Level level, ContextKeySet paramSet) {
		this.level = level;
		this.paramSet = paramSet;
	}

	public Map<ContextKey<?>, @Nullable Object> params() {
		return params;
	}

	public ContextKeySet paramSet() {
		return paramSet;
	}

	/**
	 * @param param The parameter to check
	 * @return Check whether the given parameter is present in this context.
	 */
	public boolean has(ContextKey<?> param) {
		return params.get(param) != null;
	}

	/**
	 * @return The value of the given parameter.
	 * @throws NoSuchElementException if the parameter is not present in this context
	 */
	public <T> T get(ContextKey<T> param) {
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
	public @Nullable <T> T getOrNull(ContextKey<T> param) {
		//noinspection unchecked
		return (T) params.computeIfAbsent(param, this::init);
	}

	public <T> void set(ContextKey<T> param, @Nullable T value) {
		params.put(param, value);
		if (validated && param == LootContextParams.ORIGIN) {
			for (ContextKey<?> initParam : LootParamInit.LOOKUP.keySet()) {
				if (initParam == LootContextParams.ORIGIN) {
					continue;
				}
				params.remove(initParam);
			}
		}
	}

	public void remove(ContextKey<?> param) {
		set(param, null);
	}

	public LootContext asLootContext() {
		initAll();
		var paramsBuilder = new LootParams.Builder((ServerLevel) level);
		params.forEach((p, o) -> {
			if (o != null) {
				//noinspection rawtypes,unchecked
				paramsBuilder.withParameter((ContextKey) p, o);
			}
		});
		var builder = new LootContext.Builder(paramsBuilder.create(LycheeLootContextParamSets.ALL));
		return builder.create(Optional.empty());
	}

	public void validate() {
		validate(paramSet);
	}

	public void validate(ContextKeySet paramSet) {
		final var difference = Sets.difference(paramSet.getRequired(), params.keySet());
		if (!difference.isEmpty()) {
			throw new IllegalArgumentException("Missing required parameters: " + difference);
		}
		validated = true;
	}

	public void initAll() {
		for (ContextKey<?> param : LootParamInit.LOOKUP.keySet()) {
			getOrNull(param);
		}
	}

	@Nullable
	@CheckReturnValue
	public Object init(ContextKey<?> param) {
		LootParamInit init = LootParamInit.LOOKUP.get(param);
		if (init != null) {
			return init.init(level, this);
		}
		return null;
	}
}