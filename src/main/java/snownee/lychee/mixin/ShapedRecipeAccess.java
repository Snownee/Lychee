package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;

@Mixin(ShapedRecipe.class)
public interface ShapedRecipeAccess {

	@Invoker
	boolean callMatches(CraftingContainer craftingContainer, int x, int y, boolean nonMirror);

	@Accessor
	ItemStack getResult();

}
