package snownee.lychee.mixin.rei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import net.minecraft.world.item.crafting.Recipe;
import snownee.lychee.compat.rei.display.ShapedCraftingDisplay;
import snownee.lychee.crafting.ShapedCraftingRecipe;

@Mixin(value = DefaultCraftingDisplay.class, remap = false)
public class DefaultCraftingDisplayMixin {

	@Inject(at = @At("HEAD"), method = "of", cancellable = true)
	private static void of(Recipe<?> recipe, CallbackInfoReturnable<DefaultCraftingDisplay<?>> ci) {
		if (recipe instanceof ShapedCraftingRecipe) {
			ci.setReturnValue(new ShapedCraftingDisplay((ShapedCraftingRecipe) recipe));
		}
	}

}
