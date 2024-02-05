package snownee.lychee.util.context;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.context.ActionContext;
import snownee.lychee.context.AnvilContext;
import snownee.lychee.context.JsonContext;
import snownee.lychee.context.LootParamsContext;
import snownee.lychee.util.input.ItemStackHolderCollection;

public interface LycheeContextKey<T> {
	LycheeContextKey<RandomSource> RANDOM = register("random");
	LycheeContextKey<Level> LEVEL = register("level");

	LycheeContextKey<ItemStackHolderCollection> ITEM = register("item");
	LycheeContextKey<LootParamsContext> LOOT_PARAMS = register("loot_params");
	LycheeContextKey<ActionContext> ACTION = register("action");
	LycheeContextKey<JsonContext> JSON = register("json");

	/**
	 * Recipe context
	 */

	LycheeContextKey<AnvilContext> ANVIL = register("anvil");

	static <T extends LycheeContextKey<?>> T register(ResourceLocation location, T object) {
		Registry.register(LycheeRegistries.CONTEXT, location, object);
		return object;
	}

	static <T> LycheeContextKey<T> register(String name) {
		return register(Lychee.id(name));
	}

	static <T> LycheeContextKey<T> register(ResourceLocation location) {
		return register(location, new LycheeContextKey<T>() {
			@Override
			public String toString() {
				return location.toString();
			}
		});
	}
}
