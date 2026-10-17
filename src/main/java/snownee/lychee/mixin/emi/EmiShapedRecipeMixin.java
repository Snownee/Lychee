package snownee.lychee.mixin.emi;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.recipe.EmiShapedRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import snownee.lychee.compat.recipeviewer.emi.recipe.EmiCraftingRemainders;
import snownee.lychee.recipes.ShapedCraftingRecipe;

@Mixin(value = EmiShapedRecipe.class, remap = false)
public class EmiShapedRecipeMixin {
	@Inject(method = "setRemainders", at = @At("HEAD"), cancellable = true)
	private static void lychee$setRemainders(List<EmiIngredient> input, CraftingRecipe recipe, CallbackInfo ci) {
		if (recipe instanceof ShapedCraftingRecipe shapedCraftingRecipe) {
			EmiCraftingRemainders.setRemainders(input, shapedCraftingRecipe);
			ci.cancel();
		}
	}
}
