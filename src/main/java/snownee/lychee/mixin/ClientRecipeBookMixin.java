package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.crafting.Recipe;
import snownee.lychee.core.recipe.type.LycheeRecipeType;

@Mixin(ClientRecipeBook.class)
public class ClientRecipeBookMixin {

	@Inject(at = @At("HEAD"), method = "getCategory", cancellable = true)
	private static void lychee_getCategory(Recipe<?> recipe, CallbackInfoReturnable<RecipeBookCategories> ci) {
		if (recipe.getType() instanceof LycheeRecipeType) {
			ci.setReturnValue(RecipeBookCategories.UNKNOWN);
		}
	}

}
