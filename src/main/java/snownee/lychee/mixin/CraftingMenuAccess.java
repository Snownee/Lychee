package snownee.lychee.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;

@Mixin(CraftingMenu.class)
public interface CraftingMenuAccess {

	@Accessor
	ContainerLevelAccess getAccess();

	@Accessor
	Player getPlayer();

}
