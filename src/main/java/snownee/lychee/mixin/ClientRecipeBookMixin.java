package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.util.recipe.LycheeRecipeType;

@Mixin(ClientRecipeBook.class)
public class ClientRecipeBookMixin {

	@Inject(at = @At("HEAD"), method = "getCategory", cancellable = true)
	private static void lychee_getCategory(final RecipeHolder<?> recipe, final CallbackInfoReturnable<RecipeBookCategories> cir) {
		if (recipe.value().getType() instanceof LycheeRecipeType) {
			cir.setReturnValue(RecipeBookCategories.UNKNOWN);
		}
	}
}
