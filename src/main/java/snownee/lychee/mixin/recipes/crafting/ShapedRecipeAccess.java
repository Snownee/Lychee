package snownee.lychee.mixin.recipes.crafting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

@Mixin(ShapedRecipe.class)
public interface ShapedRecipeAccess {
	@Accessor
	ShapedRecipePattern getPattern();

	@Accessor
	ItemStack getResult();
}
