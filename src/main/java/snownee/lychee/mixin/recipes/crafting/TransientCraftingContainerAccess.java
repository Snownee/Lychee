package snownee.lychee.mixin.recipes.crafting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;

@Mixin(TransientCraftingContainer.class)
public interface TransientCraftingContainerAccess {

	@Accessor
	AbstractContainerMenu getMenu();

}
