package snownee.lychee;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import snownee.kiwi.Mod;
import snownee.lychee.compat.fabric_recipe_api.AlwaysTrueIngredient;
import snownee.lychee.util.LUtil;

@Mod(Lychee.ID)
public final class Lychee implements ModInitializer {
	public static final String ID = "lychee";

	public static final Logger LOGGER = LogUtils.getLogger();

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
		CustomIngredientSerializer.register(AlwaysTrueIngredient.SERIALIZER);
	}

}
