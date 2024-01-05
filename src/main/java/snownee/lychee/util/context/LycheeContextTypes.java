package snownee.lychee.util.context;

import net.minecraft.core.Registry;
import snownee.lychee.LycheeRegistries;
import snownee.lychee.context.ActionContext;
import snownee.lychee.context.LootParamsContext;

public final class LycheeContextTypes {
	public static final LycheeContextType<LootParamsContext> LOOT_PARAMS = register(
			"loot_params",
			new LootParamsContext.Type()
	);
	public static final LycheeContextType<ActionContext> ACTION = register("action", new ActionContext.Type());

	public static <T extends LycheeContextType<?>> T register(String name, T object) {
		Registry.register(LycheeRegistries.CONTEXT, name, object);
		return object;
	}
}
