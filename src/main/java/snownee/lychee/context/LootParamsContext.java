package snownee.lychee.context;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.LycheeLootContextParamSets;
import snownee.lychee.LycheeLootContextParams;
import snownee.lychee.util.context.LycheeContext;
import snownee.lychee.util.context.LycheeContextType;
import snownee.lychee.util.context.LycheeContextValue;

public record LootParamsContext(LycheeContext context, Map<LootContextParam<?>, Object> params)
		implements LycheeContextValue<LootParamsContext> {
	/**
	 * Init {@link LootContextParams.BLOCK_ENTITY} from current {@link LycheeLootContextParams.BLOCK_POS} and
	 * {@link LootContextParams.ORIGIN}
	 */
	public void initBlockEntityParam() {
		if (params.containsKey(LootContextParams.BLOCK_ENTITY)) return;
		var pos = getOrNull(LycheeLootContextParams.BLOCK_POS);
		if (pos == null) {
			pos = BlockPos.containing(get(LootContextParams.ORIGIN));
			setParam(LycheeLootContextParams.BLOCK_POS, pos);
		}
		var blockEntity = context.get(LycheeContextType.GENERIC).level().getBlockEntity(pos);
		if (blockEntity != null) setParam(LootContextParams.BLOCK_ENTITY, blockEntity);
	}

	private void initBlockEntityParam(LootContextParam<?> param) {
		if (param == LootContextParams.BLOCK_ENTITY) initBlockEntityParam();
	}

	/**
	 * @param param The parameter to check
	 *
	 * @return Check whether the given parameter is present in this context.
	 */
	public boolean contains(LootContextParam<?> param) {
		initBlockEntityParam(param);
		return params.containsKey(param);
	}

	/**
	 * @return The value of the given parameter.
	 *
	 * @throws NoSuchElementException if the parameter is not present in this context
	 */
	public <T> T get(LootContextParam<T> param) {
		initBlockEntityParam(param);
		//noinspection unchecked
		final var result = (T) params.get(param);
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
		initBlockEntityParam(param);
		//noinspection unchecked
		return (T) params.get(param);
	}

	public <T> void setParam(LootContextParam<T> param, T value) {
		params.put(param, value);
	}

	public void removeParam(LootContextParam<?> param) {
		params.remove(param);
	}

	public LootContext asLootContext() {
		initBlockEntityParam();
		var paramsBuilder = new LootParams.Builder((ServerLevel) context.get(LycheeContextType.GENERIC).level());
		//noinspection rawtypes,unchecked
		params.forEach((p, o) -> paramsBuilder.withParameter((LootContextParam) p, o));
		var builder = new LootContext.Builder(paramsBuilder.create(LycheeLootContextParamSets.ALL));
		return builder.create(Optional.empty());
	}

	@Override
	public LycheeContextType<LootParamsContext> type() {
		return LycheeContextType.LOOT_PARAMS;
	}
}
