package snownee.lychee.mixin.rei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCraftingDisplay;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import snownee.lychee.compat.rei.display.ShapedCraftingDisplay;
import snownee.lychee.recipes.ShapedCraftingRecipe;

@Mixin(value = DefaultCraftingDisplay.class, remap = false)
public class DefaultCraftingDisplayMixin {

	@Inject(at = @At("HEAD"), method = "of", cancellable = true)
	private static void of(final RecipeHolder<? extends Recipe<?>> holder, final CallbackInfoReturnable<DefaultCraftingDisplay<?>> cir) {
		if (holder.value() instanceof ShapedCraftingRecipe) {
			cir.setReturnValue(new ShapedCraftingDisplay((RecipeHolder<ShapedCraftingRecipe>) holder));
		}
	}
}
