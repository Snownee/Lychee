package snownee.lychee.context;

import java.util.Map;
import java.util.NoSuchElementException;

import org.jspecify.annotations.Nullable;

import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.LootContext;

public interface LootParamsAccess {
	Map<ContextKey<?>, @Nullable Object> params();

	ContextKeySet paramSet();

	/**
	 * @param param The parameter to check
	 * @return Check whether the given parameter is present in this context.
	 */
	boolean has(ContextKey<?> param);

	/**
	 * @return The value of the given parameter.
	 * @throws NoSuchElementException if the parameter is not present in this context
	 */
	<T> T get(ContextKey<T> param);

	/**
	 * @return The value of the given parameter if it is present in this context, null otherwise.
	 */
	<T> @Nullable T getOrNull(ContextKey<T> param);

	<T> void set(ContextKey<T> param, @Nullable T value);

	void remove(ContextKey<?> param);

	LootContext asLootContext();

	void initAll();
}
