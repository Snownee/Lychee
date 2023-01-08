package snownee.lychee.compat.ingredient_extension;

import com.faux.ingredientextension.api.ingredient.IngredientHelper;

import net.minecraft.resources.ResourceLocation;
import snownee.lychee.Lychee;

public class IngredientExtensionCompat {

	public static void init() {
		IngredientHelper.register(new ResourceLocation(Lychee.ID, "always_true"), AlwaysTrueIngredient.Serializer.INSTANCE);
	}

}
