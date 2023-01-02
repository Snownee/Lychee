package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;

@Mixin(CraftingContainer.class)
public interface CraftingContainerAccess {

	@Accessor
	AbstractContainerMenu getMenu();

}
