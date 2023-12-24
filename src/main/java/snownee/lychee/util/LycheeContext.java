package snownee.lychee.util;

import java.util.Map;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.LycheeLootContextParams;

public interface LycheeContext {
	RandomSource random();

	Level level();

	default ServerLevel serverLevel() {
		return (ServerLevel) level();
	}

	Map<LootContextParam<?>, Object> params();

	/**
	 * Init {@link LootContextParams.BLOCK_ENTITY} from current {@link LycheeLootContextParams.BLOCK_POS} and
	 * {@link LootContextParams.ORIGIN}
	 */
	default void initBlockEntityParam() {
		if (params().containsKey(LootContextParams.BLOCK_ENTITY)) return;
		BlockPos pos = getOrNull(LycheeLootContextParams.BLOCK_POS);
		if (pos == null) {
			pos = BlockPos.containing(get(LootContextParams.ORIGIN));
			setParam(LycheeLootContextParams.BLOCK_POS, pos);
		}
		BlockEntity blockEntity = level().getBlockEntity(pos);
		if (blockEntity != null) setParam(LootContextParams.BLOCK_ENTITY, blockEntity);
	}

	/**
	 * @param param The parameter to check
	 *
	 * @return Check whether the given parameter is present in this context.
	 */
	default boolean contains(LootContextParam<?> param) {
		if (param == LootContextParams.BLOCK_ENTITY) initBlockEntityParam();
		return params().containsKey(param);
	}

	/**
	 * @return The value of the given parameter.
	 *
	 * @throws NoSuchElementException if the parameter is not present in this context
	 */
	default <T> T get(LootContextParam<T> param) {
		if (param == LootContextParams.BLOCK_ENTITY) initBlockEntityParam();
		//noinspection unchecked
		T result = (T) params().get(param);
		if (result == null) {
			throw new NoSuchElementException(param.getName().toString());
		} else {
			return result;
		}
	}

	/**
	 * @return The value of the given parameter if it is present in this context, null otherwise.
	 */
	@Nullable
	default <T> T getOrNull(LootContextParam<T> param) {
		if (param == LootContextParams.BLOCK_ENTITY) initBlockEntityParam();
		//noinspection unchecked
		return (T) params().get(param);
	}

	default <T> void setParam(LootContextParam<T> param, T value) {
		params().put(param, value);
	}

	default void removeParam(LootContextParam<?> param) {
		params().remove(param);
	}

	LootContext asLootContext();
}
