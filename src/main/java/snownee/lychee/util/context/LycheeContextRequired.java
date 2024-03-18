package snownee.lychee.util.context;

import java.util.IdentityHashMap;
import java.util.function.Function;

import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import snownee.lychee.Lychee;
import snownee.lychee.context.ActionContext;
import snownee.lychee.context.LootParamsContext;
import snownee.lychee.util.CommonProxy;
import snownee.lychee.util.action.ActionData;
import snownee.lychee.util.action.ActionMarker;
import snownee.lychee.util.input.ItemStackHolderCollection;
import snownee.lychee.util.recipe.ILycheeRecipe;

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

	public static final Function<LycheeContext, ActionMarker> MARKER = register(LycheeContextKey.MARKER, it -> {
		var level = it.get(LycheeContextKey.LEVEL);
		var marker = EntityType.MARKER.create(level);
		var lootParamsContext = it.get(LycheeContextKey.LOOT_PARAMS);
		var pos = lootParamsContext.getOrNull(LootContextParams.ORIGIN);
		if (pos != null) {
			marker.moveTo(pos);
		}
		marker.setCustomName(Component.literal(Lychee.ID));
		level.addFreshEntity(marker);
		var actionMarker = (ActionMarker) marker;
		actionMarker.lychee$setData(new ActionData(it, 0));
		return actionMarker;
	});

	public static final Function<LycheeContext, ? extends ILycheeRecipe<?>> RECIPE =
			register(LycheeContextKey.RECIPE, it -> {
				var id = it.get(LycheeContextKey.RECIPE_ID).id();
				if (id != null) {
					var holder = CommonProxy.recipe(id);
					if (holder != null && holder.value() instanceof ILycheeRecipe<?> lycheeRecipe) {
						return lycheeRecipe;
					}
				}
				return null;
			});


	public static <T> Function<LycheeContext, T> register(
			LycheeContextKey<T> key,
			Function<LycheeContext, T> constructor
	) {
		CONSTRUCTORS.put(key, constructor);
		return constructor;
	}
}
