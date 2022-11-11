package snownee.lychee;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import snownee.lychee.util.LUtil;

public final class Lychee implements ModInitializer {
	public static final String ID = "lychee";

	public static final Logger LOGGER = LogManager.getLogger(Lychee.ID);

	public static boolean hasKiwi;
	public static boolean hasIngredientExtAPI;

	@Override
	public void onInitialize() {
		hasKiwi = LUtil.isModLoaded("kiwi");
		hasIngredientExtAPI = LUtil.isModLoaded("ingredient-extension-api");
		RecipeTypes.init();
		LycheeTags.init();
		LycheeRegistries.init();
		ContextualConditionTypes.init();
		PostActionTypes.init();
		RecipeSerializers.init();
	}

}
