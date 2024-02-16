package snownee.lychee.util.context;

import java.util.IdentityHashMap;
import java.util.function.Function;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import snownee.lychee.context.ActionContext;
import snownee.lychee.context.LootParamsContext;
import snownee.lychee.util.input.ItemStackHolderCollection;

public final class LycheeContextRequired {
	public static final IdentityHashMap<LycheeContextKey<?>, Function<LycheeContext, ?>> CONSTRUCTORS =
			new IdentityHashMap<>();

	public static final Function<LycheeContext, RandomSource> RANDOM = register(
			LycheeContextKey.RANDOM,
			it -> it.get(LycheeContextKey.LEVEL).random
	);

	public static final Function<LycheeContext, Level> LEVEL = register(
			LycheeContextKey.LEVEL,
			it -> {throw new IllegalStateException("There isn't valid level in context!");}
	);

	public static final Function<LycheeContext, LootParamsContext> LOOT_PARAMS = register(
			LycheeContextKey.LOOT_PARAMS,
			it -> new LootParamsContext(it, new IdentityHashMap<>())
	);

	public static final Function<LycheeContext, ActionContext> ACTION = register(
			LycheeContextKey.ACTION,
			it -> new ActionContext()
	);

	public static final Function<LycheeContext, ItemStackHolderCollection> ITEM =
			register(LycheeContextKey.ITEM, it -> ItemStackHolderCollection.EMPTY);


	public static <T> Function<LycheeContext, T> register(
			LycheeContextKey<T> key,
			Function<LycheeContext, T> constructor
	) {
		CONSTRUCTORS.put(key, constructor);
		return constructor;
	}
}
