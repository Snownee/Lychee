package snownee.lychee.util.context;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import snownee.lychee.Lychee;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.context.ActionContext;
import snownee.lychee.context.GenericContext;
import snownee.lychee.context.ItemContext;
import snownee.lychee.context.JsonContext;
import snownee.lychee.context.LootParamsContext;
import snownee.lychee.context.recipes.AnvilContext;

public interface LycheeContextType<T extends LycheeContextValue<?>> {
	LycheeContextType<GenericContext> GENERIC = register("generic");
	LycheeContextType<ItemContext> ITEM = register("item");
	LycheeContextType<LootParamsContext> LOOT_PARAMS = register("loot_params");
	LycheeContextType<ActionContext> ACTION = register("action", new ActionContext.Type());
	LycheeContextType<JsonContext> JSON = register("json", new JsonContext.Type());

	/**
	 * Recipe context
	 */

	LycheeContextType<AnvilContext> ANVIL = register("anvil");

	static <T extends LycheeContextType<?>> T register(String name, T object) {
		return register(Lychee.id(name), object);
	}

	static <T extends LycheeContextType<?>> T register(ResourceLocation location, T object) {
		Registry.register(LycheeRegistries.CONTEXT, location, object);
		return object;
	}

	static <T extends LycheeContextValue<T>> LycheeContextType<T> register(String name) {
		return register(Lychee.id(name));
	}

	static <T extends LycheeContextValue<T>> LycheeContextType<T> register(ResourceLocation location) {
		return register(location, new LycheeContextType<T>() {
			@Override
			public String toString() {
				return location.toString();
			}
		});
	}
}
