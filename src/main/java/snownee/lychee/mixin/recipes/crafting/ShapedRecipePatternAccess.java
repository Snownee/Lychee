package snownee.lychee.mixin.recipes.crafting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

@Mixin(ShapedRecipePattern.class)
public interface ShapedRecipePatternAccess {
	@Invoker
	boolean callMatches(CraftingContainer craftingContainer, int x, int y, boolean nonMirror);
}
