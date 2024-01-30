package snownee.lychee.util.context;

import net.minecraft.core.Registry;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.context.ActionContext;
import snownee.lychee.context.GenericContext;
import snownee.lychee.context.ItemContext;
import snownee.lychee.context.JsonContext;
import snownee.lychee.context.LootParamsContext;
import snownee.lychee.context.recipes.AnvilContext;

public final class LycheeContextTypes {
	public static final LycheeContextType<GenericContext> GENERIC = register("generic", new GenericContext.Type());
	public static final LycheeContextType<ItemContext> ITEM = register("item", new ItemContext.Type());
	public static final LycheeContextType<LootParamsContext> LOOT_PARAMS = register(
			"loot_params",
			new LootParamsContext.Type()
	);
	public static final LycheeContextType<ActionContext> ACTION = register("action", new ActionContext.Type());
	public static final LycheeContextType<JsonContext> JSON = register("json", new JsonContext.Type());

	/**
	 * Recipe context
	 */

	public static final LycheeContextType<AnvilContext> ANVIL = register("anvil", new AnvilContext.Type());

	public static <T extends LycheeContextType<?>> T register(String name, T object) {
		Registry.register(LycheeRegistries.CONTEXT, name, object);
		return object;
	}
}
